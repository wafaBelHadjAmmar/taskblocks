package taskblocks.modelimpl;

public class UndoActionTaskModify implements UndoAction {
	
	TaskImpl _before;
	TaskImpl _after;
	TaskImpl _origAfter;
	TaskModelImpl _model;
	
	public UndoActionTaskModify(TaskModelImpl model, TaskImpl before, TaskImpl after) {
		_before = before;
		_after = after.clone();
		_origAfter = after;
		_model = model;
	}

	public String getUndoLabel() {
		return "change '" + _after.getName() + "'";
	}

	public String getRedoLabel() {
		return "change '" + _before.getName() + "'";
	}

	public void redo() {
		for(Object t: _model.getTasks()) {
			if(_origAfter == t) {
				((TaskImpl)t).updateFrom(_after);
			}
		}
	}

	public void undo() {
		for(Object t: _model.getTasks()) {
			if(_origAfter == t) {
				((TaskImpl)t).updateFrom(_before);
			}
		}
	}
	
	public String toString() {
		return getUndoLabel();
	}
}
