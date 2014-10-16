package org.eclipse.ease.lang.groovy.interpreter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.ease.Activator;
import org.eclipse.ease.Logger;
import org.eclipse.ease.modules.AbstractModuleWrapper;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.modules.IScriptFunctionModifier;

public class GroovyModuleWrapper extends AbstractModuleWrapper {

	public static List<String> RESERVED_KEYWORDS = new ArrayList<String>();

	static {
		RESERVED_KEYWORDS.add("abstract");
		RESERVED_KEYWORDS.add("as");
		RESERVED_KEYWORDS.add("assert");
		RESERVED_KEYWORDS.add("boolean");
		RESERVED_KEYWORDS.add("break");
		RESERVED_KEYWORDS.add("byte");
		RESERVED_KEYWORDS.add("case");
		RESERVED_KEYWORDS.add("catch");
		RESERVED_KEYWORDS.add("char");
		RESERVED_KEYWORDS.add("class");
		RESERVED_KEYWORDS.add("const");
		RESERVED_KEYWORDS.add("continue");
		RESERVED_KEYWORDS.add("def");
		RESERVED_KEYWORDS.add("default");
		RESERVED_KEYWORDS.add("do");
		RESERVED_KEYWORDS.add("double");
		RESERVED_KEYWORDS.add("else");
		RESERVED_KEYWORDS.add("enum");
		RESERVED_KEYWORDS.add("extends");
		RESERVED_KEYWORDS.add("false");
		RESERVED_KEYWORDS.add("final");
		RESERVED_KEYWORDS.add("finally");
		RESERVED_KEYWORDS.add("float");
		RESERVED_KEYWORDS.add("for");
		RESERVED_KEYWORDS.add("goto");
		RESERVED_KEYWORDS.add("if");
		RESERVED_KEYWORDS.add("implements");
		RESERVED_KEYWORDS.add("import");
		RESERVED_KEYWORDS.add("in");
		RESERVED_KEYWORDS.add("instanceof");
		RESERVED_KEYWORDS.add("int");
		RESERVED_KEYWORDS.add("interface");
		RESERVED_KEYWORDS.add("long");
		RESERVED_KEYWORDS.add("native");
		RESERVED_KEYWORDS.add("new");
		RESERVED_KEYWORDS.add("null");
		RESERVED_KEYWORDS.add("package");
		RESERVED_KEYWORDS.add("private");
		RESERVED_KEYWORDS.add("protected");
		RESERVED_KEYWORDS.add("public");
		RESERVED_KEYWORDS.add("return");
		RESERVED_KEYWORDS.add("short");
		RESERVED_KEYWORDS.add("static");
		RESERVED_KEYWORDS.add("strictfp");
		RESERVED_KEYWORDS.add("super");
		RESERVED_KEYWORDS.add("switch");
		RESERVED_KEYWORDS.add("synchronized");
		RESERVED_KEYWORDS.add("this");
		RESERVED_KEYWORDS.add("threadsafe");
		RESERVED_KEYWORDS.add("throw");
		RESERVED_KEYWORDS.add("throws");
		RESERVED_KEYWORDS.add("transient");
		RESERVED_KEYWORDS.add("true");
		RESERVED_KEYWORDS.add("try");
		RESERVED_KEYWORDS.add("void");
		RESERVED_KEYWORDS.add("volatile");
		RESERVED_KEYWORDS.add("while");
	}

	private static boolean isValidMethodName(final String methodName) {
		return isSaveName(methodName) && !RESERVED_KEYWORDS.contains(methodName);
	}

	@Override
	public String classInstantiation(final Class<?> clazz, final String[] parameters) {
		StringBuilder code = new StringBuilder();
		code.append("import ");
		code.append(clazz.getName());
		code.append(";\n");

		code.append("new ");
		code.append(clazz.getName());
		code.append("(");

		if (parameters != null) {
			for (String parameter : parameters) {
				code.append('"');
				code.append(parameter);
				code.append('"');
				code.append(", ");
			}
			if (parameters.length > 0)
				code.replace(code.length() - 2, code.length(), "");
		}

		code.append(")");

		return code.toString();
	}

	@Override
	public String createStaticFieldWrapper(final IEnvironment environment, final String moduleVariable, final Field field) {
		StringBuilder groovyCode = new StringBuilder();
		groovyCode.append("import ");
		groovyCode.append(field.getDeclaringClass().getName());
		groovyCode.append(";\n");

		groovyCode.append(getSaveVariableName(field.getName()));
		groovyCode.append(" = ");
		groovyCode.append(moduleVariable);
		groovyCode.append(".");
		groovyCode.append(field.getName());
		groovyCode.append(";\n");

		return groovyCode.toString();
	}

	@Override
	protected String getNullString() {
		return "null";
	}

	@Override
	public String getSaveVariableName(final String variableName) {
		return getSaveName(variableName);
	}

	private StringBuilder verifyParameters(final List<Parameter> parameters) {
		StringBuilder data = new StringBuilder();

		// FIXME currently not supported
		// if (!parameters.isEmpty()) {
		// Parameter parameter = parameters.get(parameters.size() - 1);
		// data.append("\tif (typeof " + parameter.getName() + " === \"undefined\") {\n");
		// if (parameter.isOptional()) {
		// data.append("\t\t" + parameter.getName() + " = " + getDefaultValue(parameter) + ";\n");
		// } else {
		// data.append("\t\tthrow new java.lang.RuntimeException('Parameter " + parameter.getName() + " is not optional');\n");
		//
		// }
		// data.append(verifyParameters(parameters.subList(0, parameters.size() - 1)));
		// data.append("\t}\n");
		// }

		return data;
	}

	@Override
	public String createFunctionWrapper(final IEnvironment environment, final String moduleVariable, final Method method) {

		StringBuilder groovyCode = new StringBuilder();

		// parse parameters
		List<Parameter> parameters = parseParameters(method);

		// build parameter string
		StringBuilder parameterList = new StringBuilder();
		for (Parameter parameter : parameters)
			parameterList.append(", ").append(parameter.getName());

		if (parameterList.length() > 2)
			parameterList.delete(0, 2);

		StringBuilder body = new StringBuilder();
		// insert parameter checks
		body.append(verifyParameters(parameters));

		// insert hooked pre execution code
		body.append(getPreExecutionCode(environment, method));

		// insert method call
		body.append("\t ").append(IScriptFunctionModifier.RESULT_NAME).append(" = ").append(moduleVariable).append(".").append(method.getName()).append("(");
		body.append(parameterList);
		body.append(");\n");

		// insert hooked post execution code
		body.append(getPostExecutionCode(environment, method));

		// insert return statement
		body.append("\treturn ").append(IScriptFunctionModifier.RESULT_NAME).append(";\n");

		// build function declarations
		for (String name : getMethodNames(method)) {
			if (!isValidMethodName(name)) {
				Logger.logError("The method name \"" + name + "\" from the module \"" + moduleVariable + "\" can not be wrapped because it's name is reserved",
						Activator.PLUGIN_ID);

			} else if (!name.isEmpty()) {
				groovyCode.append(name).append(" = { ").append(parameterList).append(" ->\n");
				groovyCode.append(body);
				groovyCode.append("}\n");
			}
		}

		return groovyCode.toString();
	}

	private static boolean isSaveName(final String identifier) {
		return Pattern.matches("[a-zA-Z_$][a-zA-Z0-9_$]*", identifier);
	}

	public static String getSaveName(final String identifier) {
		// check if name is already valid
		if (isSaveName(identifier))
			return identifier;

		// not valid, convert string to valid format
		final StringBuilder buffer = new StringBuilder(identifier.replaceAll("[^a-zA-Z0-9]", "_"));

		// remove '_' at the beginning
		while ((buffer.length() > 0) && (buffer.charAt(0) == '_'))
			buffer.deleteCharAt(0);

		// remove trailing '_'
		while ((buffer.length() > 0) && (buffer.charAt(buffer.length() - 1) == '_'))
			buffer.deleteCharAt(buffer.length() - 1);

		// check for valid first character
		if (buffer.length() > 0) {
			final char start = buffer.charAt(0);
			if ((start < 65) || ((start > 90) && (start < 97)) || (start > 122))
				buffer.insert(0, '_');
		} else
			// buffer is empty
			buffer.insert(0, '_');

		return buffer.toString();
	}
}
