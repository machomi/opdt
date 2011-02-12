package pl.com.openway.opdt.wizards;

import java.util.ArrayList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.search.IDLTKSearchScope;
import org.eclipse.dltk.core.search.SearchEngine;
import org.eclipse.dltk.ui.dialogs.FilteredTypesSelectionDialog;
import org.eclipse.dltk.ui.dialogs.ITypeInfoFilterExtension;
import org.eclipse.dltk.ui.dialogs.ITypeInfoRequestor;
import org.eclipse.dltk.ui.dialogs.TypeSelectionExtension;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.php.core.compiler.PHPFlags;
import org.eclipse.php.internal.core.PHPLanguageToolkit;
import org.eclipse.php.internal.core.documentModel.dom.IImplForPhp;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.ResourceSelectionDialog;

import pl.com.openway.opdt.Activator;

public class NewPHPInterfaceWizardPage extends WizardPage {
	private Text containerText;

	private Text fileText;

	private Text interfaceNameText;

	private ISelection selection;

	private List interfacesList;

	private IProject project;

	private List includesList;

	private ArrayList<IResource> includes;

	private ArrayList<IType> interfaces;

	private Button addInterfaceButton;

	private Button removeInterfaceButton;

	private Button addIncludeButton;

	private Button removeIncludeButton;

	private Button commentsCheckBox;

	@SuppressWarnings("unused")
	private Label filler;

	/**
	 * Constructor for NewPHPClassWizardPage.
	 *
	 * @param pageName
	 */
	public NewPHPInterfaceWizardPage(ISelection selection) {
		super("wizardPage");
		setTitle("New PHP Interface File");
		setDescription("Create new PHP Interface.");
		setImageDescriptor(Activator.getImageDescriptor("icons/newint_wiz.png"));
		this.selection = selection;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {

		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;

		Label label = new Label(container, SWT.NULL);
		label.setText("&Container:");

		containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		containerText.setLayoutData(gd);
		containerText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		Button button = new Button(container, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
		label = new Label(container, SWT.NULL);
		label.setText("&File name:");

		fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fileText.setLayoutData(gd);
		fileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		// ---------- Separator section ---------------------------

		Label separator = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment = GridData.FILL;
		gd.verticalAlignment = GridData.BEGINNING;
		gd.horizontalSpan = 3;
		separator.setLayoutData(gd);

		// ---------- Interface name section ---------------------------

		label = new Label(container, SWT.NULL);
		label.setText("&Name:");

		interfaceNameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		interfaceNameText.setLayoutData(gd);
		interfaceNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				// if (fileText.getText() == null){
				fileText.setText(interfaceNameText.getText() + ".php");
				// }
				dialogChanged();
			}
		});

		filler = new Label(container, SWT.NONE);

		// ---------- Interfaces section -----------------------------
		interfaces = new ArrayList<IType>();

		label = new Label(container, SWT.NONE);
		label.setText("Extend interfaces:");

		interfacesList = new List(container, SWT.BORDER);
		gd = new GridData(GridData.FILL_BOTH);
		interfacesList.setLayoutData(gd);

		createInterfacesButtons(container);

		interfacesList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (interfacesList.getSelectionCount() > 0) {
					removeInterfaceButton.setEnabled(true);
				} else {
					removeInterfaceButton.setEnabled(false);
				}
			}
		});

		// ---------- Includes section -------------------------------
		label = new Label(container, SWT.NONE);
		label.setText("Includes:");

		includesList = new List(container, SWT.BORDER);
		gd = new GridData(GridData.FILL_BOTH);
		includesList.setLayoutData(gd);

		createIncludesButtons(container);

		includesList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (includesList.getSelectionCount() > 0) {
					removeIncludeButton.setEnabled(true);
				} else {
					removeIncludeButton.setEnabled(false);
				}
			}
		});

		// ---------- Methods, comments section ----------------------

		label = new Label(container, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		label.setLayoutData(gd);
		label.setText("Which method stub would you like to create?");

		label = new Label(container, SWT.NONE);
		commentsCheckBox = new Button(container, SWT.CHECK);
		commentsCheckBox.setText("&Generate comments");

		initialize();
		dialogChanged();
		setControl(container);
	}

	private void createInterfacesButtons(Composite container) {

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		GridData gridData = new GridData();
		gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;

		Composite composite2 = new Composite(container, SWT.NONE);
		composite2.setLayout(gridLayout);
		composite2.setLayoutData(gridData);

		addInterfaceButton = new Button(composite2, SWT.NONE);
		addInterfaceButton.setText("&Add...");
		addInterfaceButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowseInterfaces();
				// MessageDialog.openInformation(getShell(), "Project", "prj: "
				// + project.getName());
			}
		});
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		addInterfaceButton.setLayoutData(gridData);

		filler = new Label(composite2, SWT.NONE);

		removeInterfaceButton = new Button(composite2, SWT.NONE);
		removeInterfaceButton.setText("&Remove");
		removeInterfaceButton.setEnabled(false);
		removeInterfaceButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int[] sel = includesList.getSelectionIndices();
				if (sel.length > 0) {
					for (int i = 0; i < sel.length; i++) {
						interfacesList.remove(sel[i]);
						interfaces.remove(sel[i]);
					}
				}
			}
		});
	}

	private void createIncludesButtons(Composite container) {

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		GridData gridData = new GridData();
		gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;

		Composite composite2 = new Composite(container, SWT.NONE);
		composite2.setLayout(gridLayout);
		composite2.setLayoutData(gridData);

		addIncludeButton = new Button(composite2, SWT.NONE);
		addIncludeButton.setText("&Add...");
		addIncludeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowseIncludes();
			}
		});
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		addIncludeButton.setLayoutData(gridData);

		filler = new Label(composite2, SWT.NONE);

		removeIncludeButton = new Button(composite2, SWT.NONE);
		removeIncludeButton.setText("&Remove");
		removeIncludeButton.setEnabled(false);
		removeIncludeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int[] sel = includesList.getSelectionIndices();
				if (sel.length > 0) {
					for (int i = 0; i < sel.length; i++) {
						includesList.remove(sel[i]);
						getIncludes().remove(sel[i]);
					}
				}
			}
		});
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */

	private void initialize() {
		if (selection != null && selection.isEmpty() == false
				&& selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() > 1) {
				return;
			}
			Object obj = ssel.getFirstElement();
			//System.out.println("zaznaczony " + obj.getClass());
			if (obj instanceof IImplForPhp) {
				obj = ((IImplForPhp)obj).getModelElement();
			}
			if (obj instanceof IModelElement) {
			  	obj = ((IModelElement)obj).getResource();
			}
			if (obj instanceof IResource) {
				project = ((IResource) obj).getProject();
				IContainer container;
				if (obj instanceof IContainer)
					container = (IContainer) obj;
				else
					container = ((IResource) obj).getParent();
				containerText.setText(container.getFullPath().toString());
			}
		}
		fileText.setText("NewInterface.php");
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */

	private void handleBrowse() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(
				getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
				"Select new file container");
		if (dialog.open() == ContainerSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				IResource r = ResourcesPlugin.getWorkspace().getRoot()
						.findMember((Path) result[0]);
				project = r.getProject();
				containerText.setText(((Path) result[0]).toString());
			}
		}
	}

	private class PHPInterfaceSelectionExtension extends TypeSelectionExtension {

		public ITypeInfoFilterExtension getFilterExtension() {

			return new ITypeInfoFilterExtension() {

				public boolean select(ITypeInfoRequestor typeInfoRequestor) {
					int i = typeInfoRequestor.getModifiers();
					return PHPFlags.isInterface(i);
				}
			};
		}
	}

	private void handleBrowseInterfaces() {
		IResource container = ResourcesPlugin.getWorkspace().getRoot()
				.findMember(new Path(getContainerName()));
		IProject project = container.getProject();

		IDLTKLanguageToolkit toolkit = PHPLanguageToolkit.getDefault();
		IDLTKSearchScope scope = SearchEngine.createSearchScope(DLTKCore
				.create(project));

		FilteredTypesSelectionDialog dialog = new FilteredTypesSelectionDialog(
				getShell(), true, getContainer(), scope, 0,
				new PHPInterfaceSelectionExtension(), toolkit);

		dialog.setInitialPattern("", 2);

		if (dialog.open() == dialog.OK) {

			Object result = dialog.getFirstResult();
			if (result instanceof IType) {
				IType interf = (IType) result;
				interfaces.add(interf);
				interfacesList.add(interf.getElementName());
			}
		}
	}

	private void handleBrowseIncludes() {

		ResourceSelectionDialog dialog = new ResourceSelectionDialog(
				getShell(), project, "Select files for includes");

		if (dialog.open() == ResourceSelectionDialog.OK) {

			Object[] result = dialog.getResult();

			includes = new ArrayList<IResource>();

			for (int i = 0; i < result.length; i++) {
				if (result[i] instanceof IResource) {
					IResource r = (IResource) result[i];
					getIncludes().add(i, r);
					includesList.add(r.getProjectRelativePath().toString());
				}
			}
		}
	}

	/**
	 * Ensures that both text fields are set.
	 */
	private void dialogChanged() {
		IResource container = ResourcesPlugin.getWorkspace().getRoot()
				.findMember(new Path(getContainerName()));
		String fileName = getFileName();

		if (getContainerName().length() == 0) {
			updateStatus("File container must be specified");
			return;
		}
		if (container == null
				|| (container.getType() & (IResource.PROJECT | IResource.FOLDER)) == 0) {
			updateStatus("File container must exist");
			return;
		}
		if (!container.isAccessible()) {
			updateStatus("Project must be writable");
			return;
		}
		if (fileName.length() == 0) {
			updateStatus("File name must be specified");
			return;
		}
		if (fileName.replace('\\', '/').indexOf('/', 1) > 0) {
			updateStatus("File name must be valid");
			return;
		}
		int dotLoc = fileName.lastIndexOf('.');
		if (dotLoc != -1) {
			String ext = fileName.substring(dotLoc + 1);
			if (ext.equalsIgnoreCase("php") == false) {
				updateStatus("File extension must be \"php\"");
				return;
			}
		}
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getContainerName() {
		return containerText.getText();
	}

	public String getFileName() {
		return fileText.getText();
	}

	public String getInterfaceName() {
		return interfaceNameText.getText();
	}

	public IProject getProject() {
		return project;
	}

	/**
	 * @return the includes
	 */
	public ArrayList<IResource> getIncludes() {
		return includes;
	}

	/**
	 * @return the interfaces
	 */
	public ArrayList<IType> getInterfaces() {
		return interfaces;
	}

	public boolean genereteComments() {
		return commentsCheckBox.getSelection();
	}

}
