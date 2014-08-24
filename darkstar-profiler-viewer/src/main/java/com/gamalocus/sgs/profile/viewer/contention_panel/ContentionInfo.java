package com.gamalocus.sgs.profile.viewer.contention_panel;

import java.util.Hashtable;
import java.util.Map;

class ContentionInfo
{
	Map<String, FailedTask> failed_tasks = new Hashtable<String, FailedTask>();
	Map<String, ContendedClass> contended_classes = new Hashtable<String, ContendedClass>();
	Map<String, ConflictingTask> conflicting_tasks = new Hashtable<String, ConflictingTask>();
	
	int count_total = 0;

	void addContentionPair(String taskname, String classname, String conflictingTaskname)
	{
		FailedTask task = getFailedTask(taskname);
		ContendedClass cl = getContendedClass(classname);
		ConflictingTask conflictingTask = getConflictingTask(conflictingTaskname);
		task.addClass(cl);
		conflictingTask.addClass(cl);
		cl.addTask(task, conflictingTask);
		task.count++;
		cl.count++;
		conflictingTask.count++;
		count_total++;
	}

	private ContendedClass getContendedClass(String classname)
	{
		ContendedClass cl = contended_classes.get(classname);
		if (cl == null)
		{
			cl = new ContendedClass(classname);
			contended_classes.put(classname, cl);
		}
		return cl;
	}

	private FailedTask getFailedTask(String taskname)
	{
		FailedTask task = failed_tasks.get(taskname);
		if (task == null)
		{
			task = new FailedTask(taskname);
			failed_tasks.put(taskname, task);
		}
		return task;
	}
	
	private ConflictingTask getConflictingTask(String taskname)
	{
		ConflictingTask task = conflicting_tasks.get(taskname);
		if (task == null)
		{
			task = new ConflictingTask(taskname);
			conflicting_tasks.put(taskname, task);
		}
		return task;
	}
	
	class ContendedClass implements Comparable<ContendedClass>
	{
		String name;
		int count;
		Map<FailedTask, Integer> failed_tasks = new Hashtable<FailedTask, Integer>();
		Map<ConflictingTask, Integer> conflicting_tasks = new Hashtable<ConflictingTask, Integer>();

		public ContendedClass(String classname)
		{
			name = classname;
		}

		public void addTask(FailedTask task, ConflictingTask conflictingTask)
		{
			Integer i = failed_tasks.get(task);
			failed_tasks.put(task, i == null ? new Integer(1) : i + 1);
			Integer ci = conflicting_tasks.get(conflictingTask);
			conflicting_tasks.put(conflictingTask, ci == null ? new Integer(1) : ci + 1);
		}

		public int compareTo(ContendedClass o)
		{
			int res = o.count - count;
			return (res != 0) ? res : name.compareTo(o.name);
		}

		@Override
		public String toString()
		{
			String[] parts = name.split("\\.");
			if(parts.length > 3)
			{
				return parts[parts.length-1];
			}
			return name;
		}
	}
	
	class FailedTask implements Comparable<FailedTask>
	{
		String name;
		int count;
		Map<ContendedClass, Integer> classes = new Hashtable<ContendedClass, Integer>();

		public FailedTask(String taskname)
		{
			name = taskname;
		}

		public void addClass(ContendedClass cl)
		{
			Integer i = classes.get(cl);
			classes.put(cl, (i == null) ? new Integer(1) : i + 1);
		}

		public int compareTo(FailedTask o)
		{
			int res = o.count - count;
			return (res != 0) ? res : name.compareTo(o.name);
		}

		@Override
		public String toString()
		{
			String[] parts = name.split("\\.");
			if(parts.length > 3)
			{
				return parts[parts.length-1];
			}
			return name;
		}
	}
	
	class ConflictingTask implements Comparable<ConflictingTask>
	{
		String name;
		int count;
		Map<ContendedClass, Integer> classes = new Hashtable<ContendedClass, Integer>();

		public ConflictingTask(String taskname)
		{
			name = taskname;
		}

		public void addClass(ContendedClass cl)
		{
			Integer i = classes.get(cl);
			classes.put(cl, (i == null) ? new Integer(1) : i + 1);
		}

		public int compareTo(ConflictingTask o)
		{
			int res = o.count - count;
			return (res != 0) ? res : name.compareTo(o.name);
		}

		@Override
		public String toString()
		{
			String[] parts = name.split("\\.");
			if(parts.length > 3)
			{
				return parts[parts.length-1];
			}
			return name;
		}
	}

	public void clear()
	{
		failed_tasks.clear();
		contended_classes.clear();
		conflicting_tasks.clear();
	}
}
