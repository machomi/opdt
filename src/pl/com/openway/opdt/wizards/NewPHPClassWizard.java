package pl.com.openway.opdt.wizards;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.dltk.core.IMethod;
import org.eclipse.dltk.core.IParameter;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.internal.core.SourceType;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.php.core.compiler.PHPFlags;
import org.eclipse.php.internal.core.typeinference.PHPModelUtils;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import pl.com.openway.opdt.utils.StringUtils;

public class NewPHPClassWizard extends Wizard implements INewWizard {
	private NewPHPClassWizardPage page;
	private ISelection selection;

	/**
	 * Constructor for NewPHPClassWizard.
	 */
	public NewPHPClassWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		page = new NewPHPClassWizardPage(selection);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We
	 * will create an operation and run it using wizard as execution context.
	 */
	public boolean performFinish() {
		// creating job is in another thread
		// so all need var must be final
		final String containerName = page.getContainerName();
		final String fileName = page.getFileName();
		final String className = page.getClassName();
		final boolean generateAbstractMethods = page.generateAbstractMethods();
		final boolean generateContructor = page.generateConstructors();
		final boolean generateComments = page.genereteComments();
		final IType superClass = page.getSuperclass();
		final ArrayList<IType> interfaces = page.getInterfaces();
		final ArrayList<IResource> includes = page.getIncludes();
		final boolean isAbstract = page.isAbstract();
		final boolean isFinal = page.isFinal();

		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
					throws InvocationTargetException {
				try {
					doFinish(containerName, fileName, className, superClass, interfaces, includes, isAbstract, isFinal, generateAbstractMethods, generateContructor, generateComments, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException
					.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * The worker method. It will find the container, create the file if missing
	 * or just replace its contents, and open the editor on the newly created
	 * file.
	 */

	private void doFinish(String containerName, String fileName, String className, IType superClass, List<IType> interfaces, List<IResource> includes, boolean isAbstract, boolean isFinal, boolean generateAbstractMethods, boolean generateContructor, boolean generateComments, IProgressMonitor monitor)
			throws CoreException {

		// create a sample file
		monitor.beginTask("Creating " + fileName, 2);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerName
					+ "\" does not exist.");
		}
		IContainer container = (IContainer) resource;
		final IFile file = container.getFile(new Path(fileName));
		try {
			InputStream stream = openContentStream(className, superClass, interfaces, includes, isAbstract, isFinal, generateAbstractMethods, generateContructor, generateComments);
			if (file.exists()) {
				file.setContents(stream, true, true, monitor);
			} else {
				file.create(stream, true, monitor);
			}
			stream.close();
		} catch (IOException e) {
		}
		monitor.worked(1);
		monitor.setTaskName("Opening file for editing...");
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, file, true);
				} catch (PartInitException e) {
				}
			}
		});
		monitor.worked(1);
	}

	/**
	 * We will initialize file contents with a sample text.
	 */
	private InputStream openContentStream(String className, IType superClass, List<IType> interfaces, List<IResource> includes, boolean isAbstract, boolean isFinal, boolean generateAbstractMethods, boolean generateContructor, boolean generateComments) {

		StringBuffer sb = new StringBuffer();
		sb.append("<?php\n");

		if (includes != null && includes.size() > 0) {
			for (Iterator iterator = includes.iterator(); iterator.hasNext();) {
				IResource iResource = (IResource) iterator.next();
				sb.append("include '" + iResource.getProjectRelativePath().toString() + "';\n");
			}
		}

		sb.append("\n");
		if (generateComments){
			sb.append("/**\n" +
					" *\n" +
					" */\n");
		}

		if (isAbstract) {
			sb.append("abstract ");
		} else if (isFinal) {
			sb.append("final ");
		}

		sb.append("class " + className);

		if (superClass != null) {
			sb.append(" extends " + superClass.getElementName());
		}
		if (interfaces != null && interfaces.size() > 0) {
			ArrayList<String> interfacesNames = new ArrayList<String>();
			for (IType iType : interfaces) {
				interfacesNames.add(iType.getElementName());
			}
			sb.append(" implements ");
			sb.append(StringUtils.implodeStrings(interfacesNames, ", "));
		}

		sb.append(" {\n");
		if (generateContructor) {
			sb.append("\n\tpublic function __construct()\n\t{\n\n\t}\n");
			sb.append("\n\tpublic function __destruct()\n\t{\n\n\t}\n");
		}

		if (generateAbstractMethods) {

			try {
				if (superClass != null && PHPFlags.isAbstract(superClass.getFlags())) {
					sb.append(getImplmentFunctionsBody(superClass, generateComments));

				}
			} catch (ModelException e) {
				e.printStackTrace();
			}

			if (interfaces != null && interfaces.size() > 0) {
				for (IType type : interfaces) {
					if (type instanceof SourceType) {
						SourceType interf = (SourceType) type;
						try {
							for(IMethod method : interf.getMethods()) {

								sb.append("\n\t");
								if (generateComments) {
									sb.append("/**\n\t *\n\t */\n\t");
								}
								//sb.append(method.getSource().replaceAll("abstract", ""));
								sb.append(getMethodBody(method));
								sb.append("\n");

							}
						} catch (ModelException e) {
							e.printStackTrace();
						}
					}
				}
			}

		}

		sb.append("\n}\n");

		return new ByteArrayInputStream(sb.toString().getBytes());
	}

	private String getImplmentFunctionsBody(IType type, boolean generateComments) {

		if (type instanceof SourceType) {

			SourceType el = ((SourceType) type);

			try {
				IMethod[] abstractMethods = PHPModelUtils
						.getUnimplementedMethods(el, null, null);

				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < abstractMethods.length; i++) {

					if (generateComments) {
						sb.append("\n\t/**\n\t *\n\t */");
					}
					sb.append("\n\t");
					//sb.append(abstractMethods[i].getSource().replaceAll("abstract", ""));
					sb.append(getMethodBody(abstractMethods[i]));
					sb.append("\n");

				}
				return sb.toString();

			} catch (ModelException e1) {
				e1.printStackTrace();
			}

		}
		return null;
	}

	private String getMethodBody(IMethod method) throws ModelException {

		StringBuffer sb = new StringBuffer();

		if (PHPFlags.isPublic(method.getFlags())) {
			sb.append("public ");
		} else if (PHPFlags.isProtected(method.getFlags())) {
			sb.append("protected ");
		}
		sb.append("function ");
		sb.append(method.getElementName());
		sb.append("(");

		ArrayList<String> params = new ArrayList<String>();
		for(IParameter parameter : method.getParameters()) {
			StringBuffer methodString = new StringBuffer();
			if (parameter.getType() != null) {
				methodString.append(parameter.getType());
				methodString.append(" ");
			}
			methodString.append(parameter.getName());
			params.add(methodString.toString());
		}
		sb.append(StringUtils.implodeStrings(params, ","));
		sb.append(")");
		sb.append("\n\t{\n\t\n\t}");

		return sb.toString();
	}

	private void throwCoreException(String message) throws CoreException {
		IStatus status = new Status(IStatus.ERROR, "pdtext", IStatus.OK,
				message, null);
		throw new CoreException(status);
	}

	/**
	 * We will accept the selection in the workbench to see if we can initialize
	 * from it.
	 *
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}
