package jess.morgan.car_data_logger.decode.can.eval;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class BuiltInJavaScriptEvalImpl implements Eval {
	private final ScriptEngine engine;

	public BuiltInJavaScriptEvalImpl() {
		ScriptEngineManager manager = new ScriptEngineManager();
		engine = manager.getEngineByName("js");
	}

	@Override
	public String eval(String script) throws EvalException {
		try {
			return engine.eval(script).toString();
		} catch (ScriptException e) {
			throw new EvalException(e);
		}
	}
}
