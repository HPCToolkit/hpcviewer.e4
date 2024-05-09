package edu.rice.cs.hpctoolcontrol;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.core.runtime.jobs.ProgressProvider;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;


/*****************************************
 * 
 * Main class to display progress bar and anything in the status bar.
 * <p>
 * This class is inspired from Eclipse Vogella blog at
 * https://www.vogella.com/tutorials/EclipseJobs/article.html
 * </p>
 *****************************************/
public class ToolControl 
{
	private final UISynchronize sync;

	private ProgressBar progressBar;
	private GobalProgressMonitor monitor;
	private Label lblMessage;
	

	@Inject
	public ToolControl(UISynchronize sync) {
		this.sync = Objects.requireNonNull(sync);
	}

	@PostConstruct
	public void createControls(Composite parent){

		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(parent);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(parent);

		var container = new Composite(parent, SWT.BORDER);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(container);

		lblMessage = new Label(container, SWT.RIGHT);

		progressBar = new ProgressBar(container, SWT.SMOOTH);
		progressBar.setBounds(100, 10, 200, 20);

		//memory = new Canvas(parent, 0);
		//memory.setBounds(100, 10, 200, 20);
		
		GridDataFactory.fillDefaults().grab(true, false).applyTo(lblMessage);		
		GridDataFactory.fillDefaults().grab(true, false).applyTo(container);
		
		monitor = new GobalProgressMonitor();

		Job.getJobManager().setProgressProvider(new ProgressProvider() {
			@Override
			public IProgressMonitor createMonitor(Job job) {
				return monitor.addJob(job);
			}
		});
	}
	
	@PreDestroy
	public void preDestroy() {
		Job.getJobManager().setProgressProvider(null);
	}

	private final class GobalProgressMonitor extends NullProgressMonitor {

		// thread-Safe via thread confinement of the UI-Thread
		// (means access only via UI-Thread)
		private AtomicInteger runningTasks = new AtomicInteger(0);

		@Override
		public void beginTask(final String name, final int totalWork) {
			
			sync.asyncExec(() -> {
				if (progressBar.isDisposed()) return;
				
				lblMessage.setText(name);

				if( runningTasks.get() <= 0 ) {
					// --- no task is running at the moment ---
					progressBar.setSelection(0);
					progressBar.setMaximum(totalWork);

				} else {
					// --- other tasks are running ---
					progressBar.setMaximum(progressBar.getMaximum() + totalWork);
				}

				var tasks = runningTasks.incrementAndGet();
				progressBar.setToolTipText("Currently running: " + tasks +
						"\nLast task: " + name);
			});
		}

		@Override
		public void worked(final int work) {
			sync.asyncExec( () -> {
				if (progressBar.isDisposed()) return;				
				progressBar.setSelection(progressBar.getSelection() + work);
			});
		}

		@Override
		public void done() {
			sync.asyncExec(() -> {
				if (!progressBar.isDisposed())
					progressBar.setSelection(0);
				if (!lblMessage.isDisposed())
					lblMessage.setText("");
			});
		}
		
		public IProgressMonitor addJob(Job job){
			if( job != null && progressBar != null && !progressBar.isDisposed()) {
				job.addJobChangeListener(new JobChangeAdapter() {
					@Override
					public void done(IJobChangeEvent event) {
						sync.asyncExec(() -> {
							var tasks = runningTasks.decrementAndGet();
							if (tasks > 0 ){
								// --- some tasks are still running ---
								progressBar.setToolTipText("Currently running: " + tasks);

							} else {
								// --- all tasks are done (a reset of selection could also be done) ---
								progressBar.setToolTipText("No background progress running.");
							}
						});

						// clean-up
						event.getJob().removeJobChangeListener(this);
					}
				});
			}
			return this;
		}
		
		@Override
		public void setTaskName(String name) {
			sync.asyncExec(() -> {
				lblMessage.setText(name);
			});
		}
	}
}