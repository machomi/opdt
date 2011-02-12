package pl.com.openway.opdt.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;

import pl.com.openway.opdt.Activator;
import pl.com.openway.opdt.wizards.NewPHPClassWizard;

public class OpenNewPHPClassWizardAction extends Action implements IWorkbenchWindowPulldownDelegate {
	private IWorkbenchWindow window;
	private IStructuredSelection fSelection;
	/**
	 * The constructor.
	 */
	public OpenNewPHPClassWizardAction() {
	}

	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		run();
	}

	public void run() {
		NewPHPClassWizard wizard = new NewPHPClassWizard();
		wizard.init(window.getWorkbench(), getSelection());

		WizardDialog wizardDialog = new WizardDialog(window.getShell(),wizard);
		wizardDialog.open();
	}

	/**
	 * Selection in the workbench has been changed. We
	 * can change the state of the 'real' action here
	 * if we want, but this can only happen after
	 * the delegate has been created.
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
	 * We can use this method to dispose of any system
	 * resources we previously allocated.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to
	 * be able to provide parent shell for the message dialog.
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
		setText("&New PHP Class");
		setToolTipText("&Create New PHP Class.");
		setImageDescriptor(Activator.getImageDescriptor("icons/newclass_wiz.gif"));
	}

	Menu fMenu;

	@Override
	public Menu getMenu(Control parent) {
		if (fMenu == null) {
			fMenu = new Menu(parent);
			OpenNewPHPInterfaceWizardAction interfaceAction = new OpenNewPHPInterfaceWizardAction();
			interfaceAction.init(window);

			ActionContributionItem item = new ActionContributionItem(interfaceAction);
			item.fill(fMenu, -1);
			item = new ActionContributionItem(this);
			item.fill(fMenu, -1);
		}
		return fMenu;
	}
}