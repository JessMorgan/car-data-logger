package jess.morgan.car_data_logger.decode_can.eval;

public interface Eval {
	public String eval(String script) throws EvalException;
}
