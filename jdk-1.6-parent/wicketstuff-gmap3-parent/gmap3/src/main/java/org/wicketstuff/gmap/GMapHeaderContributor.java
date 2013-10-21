package org.wicketstuff.gmap;

import org.apache.wicket.ajax.WicketAjaxReference;
import org.apache.wicket.behavior.AbstractHeaderContributor;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WicketEventReference;

public class GMapHeaderContributor extends AbstractHeaderContributor {
	/** Serial ID. */
	private static final long serialVersionUID = 1L;
	// URL for Google Maps' API endpoint.
	private static final String GMAP_API_URL = "%s://maps.google.com/maps/api/js?v=3&amp;sensor=%s";
	/** HTTP scheme. */
	private static final String HTTP = "http";
	/** Scheme to use (http or https). */
	private final String scheme;
	/** using sensor? */
	private String sensor = "false";
	/** header contributer. */
	private IHeaderContributor headerContributor = null;

	public GMapHeaderContributor() {
		this(HTTP, false);
	}

	public GMapHeaderContributor(final boolean sensor) {
		this(HTTP, sensor);
	}

	public GMapHeaderContributor(final String scheme) {
		this(scheme, false);
	}

	/**
	 * Constructor.
	 * 
	 * Should be added to the page.
	 * 
	 * @param scheme http or https?
	 * @param sensor
	 */
	public GMapHeaderContributor(final String scheme, final boolean sensor) {
		this.scheme = scheme;
		if (sensor) {
			this.sensor = "true";
		}

		headerContributor = new IHeaderContributor() {
			private static final long serialVersionUID = 1L;

			/**
			 * @see org.apache.wicket.markup.html.IHeaderContributor#renderHead(org.apache.wicket.markup.html.IHeaderResponse)
			 */
			@Override
			public void renderHead(final IHeaderResponse response) {
				// les wicket
				response.renderJavascriptReference(WicketEventReference.INSTANCE);
				response.renderJavascriptReference(WicketAjaxReference.INSTANCE);
				// gmap
				response.renderJavascriptReference(String.format(GMAP_API_URL, scheme, sensor));
				// gmap/wicket
				response.renderJavascriptReference(WicketGMapJsReference.INSTANCE);
			}
		};
	}

	public String getSensor() {
		return sensor;
	}

	@Override
	public IHeaderContributor[] getHeaderContributors() {
		return new IHeaderContributor[] { headerContributor };
	}

	/**
	 * @return the headerContributor
	 */
	protected final IHeaderContributor getHeaderContributor() {
		return headerContributor;
	}

	/**
	 * @param varHeaderContributor the headerContributor to set
	 */
	protected final void setHeaderContributor(final IHeaderContributor varHeaderContributor) {
		headerContributor = varHeaderContributor;
	}
}
