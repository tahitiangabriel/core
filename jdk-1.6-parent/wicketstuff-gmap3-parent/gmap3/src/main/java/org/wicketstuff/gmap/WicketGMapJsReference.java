package org.wicketstuff.gmap;

import org.apache.wicket.markup.html.resources.JavascriptResourceReference;

/**
 * A JavaScript reference for wicket-gmap.js
 */
public class WicketGMapJsReference extends JavascriptResourceReference {

	private static final long serialVersionUID = 1L;
	public static final WicketGMapJsReference INSTANCE = new WicketGMapJsReference();

	/**
	 * Constructor.
	 */
	private WicketGMapJsReference() {
		super(WicketGMapJsReference.class, "wicket-gmap.js");
	}
}
