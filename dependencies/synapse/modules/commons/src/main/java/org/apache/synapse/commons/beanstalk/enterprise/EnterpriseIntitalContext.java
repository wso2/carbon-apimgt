package org.apache.synapse.commons.beanstalk.enterprise;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;

/**
 * Enterprise specific Initial context which is avoiding allows to setup the
 * user defined properties
 * to the context
 * 
 */
public class EnterpriseIntitalContext extends InitialContext {

	public EnterpriseIntitalContext() throws NamingException {
		super();

	}

	/**
	 * Constructs an initial context using the supplied environment.
	 * Environment properties are discussed in the class description.
	 * 
	 * <p>
	 * This constructor will not modify <tt>environment</tt> or save a reference
	 * to it, but may save a clone.
	 * 
	 * @param environment
	 *            environment used to create the initial context.
	 *            Null indicates an empty environment.
	 * 
	 * @throws NamingException
	 *             if a naming exception is encountered
	 */
	public EnterpriseIntitalContext(Hashtable<?, ?> environment) throws NamingException {
		if (environment != null) {
			environment = (Hashtable) environment.clone();
		}
		init(environment);
	}

	/* (non-Javadoc)
	 * @see javax.naming.InitialContext#getURLOrDefaultInitCtx(java.lang.String)
	 */
	@Override
	protected Context getURLOrDefaultInitCtx(String name) throws NamingException {
		String scheme = getURLScheme(name);
		if (scheme != null) {
			Context ctx = NamingManager.getURLContext(scheme, myProps);
			if (ctx != null) {
				return ctx;
			}
		}
		return getDefaultInitCtx();
	}

	private static String getURLScheme(String str) {
		int colon_posn = str.indexOf(':');
		int slash_posn = str.indexOf('/');

		if (colon_posn > 0 && (slash_posn == -1 || colon_posn < slash_posn))
			return str.substring(0, colon_posn);
		return null;
	}

}
