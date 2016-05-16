package syslog;

import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public class SyslogMessageParser {
	private static final String SYSLOG_NS = "syslog.core";
	private final Var cljParse;
	public SyslogMessageParser() {
		RT.var("clojure.core", "require").invoke(Symbol.intern(SYSLOG_NS));
		cljParse = RT.var(SYSLOG_NS, "parse");
	}
	public SyslogMessage parse(final String msg) {
		return new SyslogMessageImpl(cljParse.invoke(msg));
	}
	
	public static void main(final String[] args) {
		System.out.println("Parsing...");
		final SyslogMessageParser slmp = new SyslogMessageParser();
		for (String a: args) System.out.println(slmp.parse(args[0]));
	}
}
