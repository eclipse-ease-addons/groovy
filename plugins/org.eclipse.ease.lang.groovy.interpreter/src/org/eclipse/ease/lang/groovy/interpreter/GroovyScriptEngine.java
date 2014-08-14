package org.eclipse.ease.lang.groovy.interpreter;

import groovy.lang.GroovyShell;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.Map;

import org.eclipse.ease.AbstractScriptEngine;
import org.eclipse.ease.Script;

public class GroovyScriptEngine extends AbstractScriptEngine {

	private GroovyShell fEngine;

	public GroovyScriptEngine() {
		super("Groovy");
	}

	@Override
	public void terminateCurrent() {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean setupEngine() {
		fEngine = new GroovyShell();

		setOutputStream(getOutputStream());
		setErrorStream(getErrorStream());

		return true;
	}

	@Override
	public void setOutputStream(final OutputStream outputStream) {
		super.setOutputStream(outputStream);

		if (fEngine != null)
			fEngine.setProperty("out", getOutputStream());

	}

	@Override
	public void setErrorStream(final OutputStream errorStream) {
		super.setOutputStream(errorStream);

		if (fEngine != null)
			fEngine.setProperty("err", getErrorStream());
	}

	@Override
	protected boolean teardownEngine() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected Object execute(final Script script, final Object reference, final String fileName, final boolean uiThread) throws Exception {
		InputStreamReader reader = null;
		Object result = null;
		try {
			reader = new InputStreamReader(script.getCodeStream());
			if ((fileName == null) || (fileName.isEmpty()))
				result = fEngine.evaluate(reader);

			else
				result = fEngine.evaluate(reader, fileName);

		} catch (Exception e) {
			throw e;
		} finally {
			// gracefully close reader
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
			}
		}

		return result;
	}

	@Override
	public void setVariable(final String name, final Object content) {
		fEngine.setVariable(name, content);
	}

	@Override
	public Object getVariable(final String name) {
		return fEngine.getVariable(name);
	}

	@Override
	public boolean hasVariable(final String name) {
		return false;
	}

	@Override
	public String getSaveVariableName(final String name) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public Object removeVariable(final String name) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public Map<String, Object> getVariables() {
		throw new RuntimeException("not implemented");
	}

	@Override
	public void registerJar(final URL url) {
		throw new RuntimeException("not implemented");
	}
}
