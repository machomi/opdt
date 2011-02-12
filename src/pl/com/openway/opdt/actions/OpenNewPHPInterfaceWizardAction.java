package pl.com.openway.opdt.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import pl.com.openway.opdt.Activator;
import pl.com.openway.opdt.wizards.NewPHPInterfaceWizard;


/**
 * Our sample action implements workbench action delegate. The action proxy will
 * be created by the workbench and shown in the UI. When the user tries to use
 * the action, this delegate will be created and execution will be delegated to
 * it.
 *
 * @see IWorkbenchWindowActionDelegate
 */
public class OpenNewPHPInterfaceWizardAction extends Action implements  IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;
	private IStructuredSelection fSelection;
	/**
	 * The constructor.
	 */
	public OpenNewPHPInterfaceWizardAction() {
	}

	/**
	 * The action has been activated. The argument of the method represents the
	 * 'real' action sitting in the workbench UI.
	 *
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		run();
	}

	public void run(){
		NewPHPInterfaceWizard wizard = new NewPHPInterfaceWizard();
		wizard.init(window.getWorkbench(), getSelection());

		WizardDialog wizardDialog = new WizardDialog(window.getShell(),wizard);
		wizardDialog.open();

	}

	/**
	 * Selection in the workbench has been changed. We can change the state of
	 * the 'real' action here if we want, but this can only happen after the
	 * delegate has been created.
	 *
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {

	}

	/**
	 * Returns the configured selection. If no selection has been configured
	 * using {@link #setSelection(IStructuredSelection)}, the currently
	 * selected element of the active workbench is returned.
	 *
	 * @return the configured selection
	 */
	protected IStructuredSelection getSelection() {
		if (fSelection == null) {
			return evaluateCurrentSelection();
		}
		return fSelection;
	}

	private IStructuredSelection evaluateCurrentSelection() {
		IWorkbenchWindow window = Activator.getDefault().getWorkbench()
				.getActiveWorkbenchWindow();
		if (window != null) {
			ISelection selection = window.getSelectionService().getSelection();
			if (selection instanceof IStructuredSelection) {
				return (IStructuredSelection) selection;
			}
		}
		return StructuredSelection.EMPTY;
	}

	/**
	 * We can use this method to dispose of any system resources we previously
	 * allocated.
	 *
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to be able to provide parent shell
	 * for the message dialog.
	 *
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
		setText("&New PHP Interface");
		setToolTipText("&Create New PHP Class.");
		setImageDescriptor(Activator.getImageDescriptor("icons/newint_wiz.gif"));
	}


}