package org.wicketstuff.gmap.api;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.util.string.Strings;
import org.wicketstuff.gmap.js.Constructor;
import org.wicketstuff.gmap.js.ObjectLiteral;

/**
 * Represents an Google Maps API's <a href= "http://www.google.com/apis/maps/documentation/reference.html#GInfoWindow"
 * >GInfoWindow</a>.
 * 
 * You can attach following events to an InfoWindow in case you want to react to one of those:
 * 
 * @see GEvent#content_changed
 * @see GEvent#domready
 * @see GEvent#closeclick
 */
public class GInfoWindow extends GOverlay {

	/**
	 * Default serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;
	private GLatLng latLng;
	private GMarker marker;
	private String content;
	private boolean contentIsNode;
	private Integer maxWidth;
	private Boolean disableAutoPan;
	private Integer zIndex;

	/**
	 * Constructor.
	 * 
	 * @param latLng the position where the info window should be opened
	 * @param content the (HTML) content which should be shown. Any containing
	 *            apostrophes will be escaped.
	 */
	public GInfoWindow(final GLatLng latLng, final String content) {
		this(latLng);
		this.content = Strings.replaceAll(content, "'", "\\'").toString();
	}

	/**
	 * Constructor.
	 * 
	 * @param latLng the position where the info window should be opened
	 * @param content the Component which should be shown. Internally uses document.getElementById('" + markupId + "')
	 *            to link to the GInfoWindow
	 */
	public GInfoWindow(final GLatLng latLng, final Component content) {
		this(latLng);

		if (content == null) {
			throw new NullPointerException("content must not be null");
		}

		content.setOutputMarkupId(true);
		final String markupId = content.getMarkupId(true);
		this.content = String.format("document.getElementById('%s')", markupId);
		contentIsNode = true;
	}

	private GInfoWindow(final GLatLng latLng) {
		super();

		if (latLng == null) {
			throw new NullPointerException("latLng must not be null");
		}
		this.latLng = latLng;
	}

	@Override
	public String getJSconstructor() {

		final ObjectLiteral args = new ObjectLiteral();
		if (!contentIsNode) {
			args.setString("content", content);
		} else {
			args.set("content", content);
		}

		args.set("position", latLng.toString());

		if (maxWidth != null) {
			args.set("maxWidth", maxWidth.toString());
		}

		if (disableAutoPan != null) {
			args.set("disableAutoPan", disableAutoPan.toString());
		}

		if (zIndex != null) {
			args.set("zIndex", zIndex.toString());
		}

		final Constructor constructor = new Constructor("google.maps.InfoWindow").add(args.toJS());
		return constructor.toJS();
	}

	/**
	 * Update state from a request to an AJAX target.
	 */
	@Override
	protected void updateOnAjaxCall(final AjaxRequestTarget target, final GEvent overlayEvent) {
	}

	public boolean isOpen() {
		return (latLng != null || marker != null);
	}

	public void close() {
		marker = null;
		latLng = null;

		final AjaxRequestTarget target = AjaxRequestTarget.get();
		if (target != null) {
			target.appendJavascript(super.getJSremove());
		}
	}

	public GLatLng getLatLng() {
		return latLng;
	}

	public void setMaxWidth(final Integer maxWidth) {
		this.maxWidth = maxWidth;
	}

	public void setLatLng(final GLatLng latLng) {
		this.latLng = latLng;
	}

	public String getContent() {
		return content;
	}

	public void setContent(final String content) {
		this.content = content;
	}

	public Integer getMaxWidth() {
		return maxWidth;
	}

	public Boolean getDisableAutoPan() {
		return disableAutoPan;
	}

	public void setDisableAutoPan(final Boolean disableAutoPan) {
		this.disableAutoPan = disableAutoPan;
	}

	public Integer getzIndex() {
		return zIndex;
	}

	public void setzIndex(final Integer zIndex) {
		this.zIndex = zIndex;
	}
}
