package org.eclipse.ease.lang.groovy.interpreter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.Map;

import org.eclipse.ease.AbstractScriptEngine;
import org.eclipse.ease.Script;
import org.eclipse.ease.ScriptEngineException;
import org.eclipse.ease.lang.groovy.GroovyHelper;

import groovy.lang.GroovyShell;

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
	protected void setupEngine() throws ScriptEngineException {
		fEngine = new GroovyShell();

		setOutputStream(getOutputStream());
		setErrorStream(getErrorStream());
	}

	@Override
	public void setOutputStream(final OutputStream outputStream) {
		super.setOutputStream(outputStream);

		if (fEngine != null)
			fEngine.setProperty("out", getOutputStream());
	}

	@Override
	public void setErrorStream(final OutputStream errorStream) {
		super.setErrorStream(errorStream);

		if (fEngine != null)
			fEngine.setProperty("err", getErrorStream());
	}

	@Override
	protected void teardownEngine() throws ScriptEngineException {
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
	protected Object internalGetVariable(final String name) {
		return fEngine.getContext().getVariable(name);
	}

	@Override
	protected Map<String, Object> internalGetVariables() {
		return fEngine.getContext().getVariables();
	}

	@Override
	protected boolean internalHasVariable(final String name) {
		return fEngine.getContext().hasVariable(name);
	}

	@Override
	protected void internalSetVariable(final String name, final Object content) {
		fEngine.getContext().setVariable(name, content);
	}

	@Override
	protected Object internalRemoveVariable(final String name) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public String getSaveVariableName(final String name) {
		return GroovyHelper.getSaveName(name);
	}

	@Override
	public void registerJar(final URL url) {
		throw new RuntimeException("not implemented");
	}
}
