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
    private static final String GMAP_API_URL = "%s://maps.google.com/maps/api/js?v=3&amp;sensor=%s%s";
    /** HTTP scheme. */
    private static final String HTTP = "http";
    /** Scheme to use (http or https). */
    private final String scheme;
    /** Key gmap. */
    private final String key;
    /** using sensor? */
    private String sensor = "false";
    /** header contributer. */
    private IHeaderContributor headerContributor = null;

    public GMapHeaderContributor() {
        this(HTTP, false, null);
    }

    public GMapHeaderContributor(final boolean sensor) {
        this(HTTP, sensor, null);
    }

    public GMapHeaderContributor(final String scheme) {
        this(scheme, false, null);
    }

    public GMapHeaderContributor(final boolean sensor, final String key) {
        this(HTTP, sensor, key);
    }

    public GMapHeaderContributor(final String scheme, final String key) {
        this(scheme, false, key);
    }

    /**
     * Constructor.
     * 
     * Should be added to the page.
     * 
     * @param scheme http or https?
     * @param sensor
     */
    public GMapHeaderContributor(final String scheme, final boolean sensor, final String key) {
        this.scheme = scheme;
        this.key = key;
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
                if (key == null || key.isEmpty()) {
                    response.renderJavascriptReference(String.format(GMAP_API_URL, scheme, sensor, ""));
                } else {
                    response.renderJavascriptReference(String.format(GMAP_API_URL, scheme, sensor, "&amp;key=" + key));
                }
                // gmap/wicket
                response.renderJavascriptReference(WicketGMapJsReference.INSTANCE);
            }
        };
    }

    public String getSensor() {
        return sensor;
    }

    /**
     * @return the scheme
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
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
