package org.wicketstuff.gmap;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.WicketAjaxReference;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WicketEventReference;

public class GMapHeaderContributor extends Behavior {

    private static final long serialVersionUID = 1L;
    // URL for Google Maps' API endpoint.
    private static final String GMAP_API_URL = "%s://maps.google.com/maps/api/js?v=3&sensor=%s%s";
    private static final String HTTP = "http";
    // We have some custom Javascript.
    private final String scheme;
    /** Key gmap. */
    private String key;
    /** using sensor? */
    private String sensor = "false";

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
    }

    @Override
    public void renderHead(final Component component, final IHeaderResponse response) {
        super.renderHead(component, response);
        // les wicket
        response.renderJavaScriptReference(WicketEventReference.INSTANCE);
        response.renderJavaScriptReference(WicketAjaxReference.INSTANCE);
        // gmap
        if (key == null || key.isEmpty()) {
            response.renderJavaScriptReference(String.format(GMAP_API_URL, scheme, sensor, ""));
        } else {
            response.renderJavaScriptReference(String.format(GMAP_API_URL, scheme, sensor, "&key=" + key));
        }
        // gmap/wicket
        response.renderJavaScriptReference(WicketGMapJsReference.INSTANCE);
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

    /**
     * @param varKey the key to set
     */
    public void setKey(final String varKey) {
        key = varKey;
    }

}
