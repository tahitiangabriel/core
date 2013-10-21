/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Wicket GMap3
 *
 * @author Tilman M�ller
 */
// Wicket Namespace
var Wicket;
if (!Wicket) {
	Wicket = {}
} else if (typeof Wicket != "object") {
	throw new Error("Wicket already exists but is not an object");
}

Wicket.geocoder = new WicketClientGeocoder();

function WicketClientGeocoder() {

    this.coder = new google.maps.Geocoder();

    this.getLatLng = function(callBack, addressId){

        var address = Wicket.$(addressId).value;

        this.coder.geocode({
            'address': address
        }, function(results, status){

            if (status === google.maps.GeocoderStatus.OK) {
                callBack = callBack + '&address=' + results[0].formatted_address;
                callBack = callBack + '&coordinates=' + results[0].geometry.location;
            }
            callBack = callBack + '&status=' + status;


				wicketAjaxGet(
						callBack, 
						function() { }, 
						function() { }
					);
        });
    }
}


Wicket.maps = {}

function WicketMap(id) {
	Wicket.maps[id] = this;

	this.options = {};
	
	this.map = new google.maps.Map(document.getElementById(id));
	this.overlays = {};
	this.singleInfoWindo = null;
	this.infoWindow = null;
	

	this.onEvent = function(callBack, params) {
		params['center'] = this.map.getCenter();
		params['bounds'] = this.map.getBounds();
		params['zoom'] = this.map.getZoom();
        params['currentMapType'] = this.getMapTypeString(this.map.getMapTypeId());

		for ( var key in params) {
			callBack = callBack + '&' + key + '=' + params[key];
		}

		wicketAjaxGet(callBack, function() {}, function() {	});
	}
	
	this.openSingleInfoWindowOn = function (overlay, contentString) {     
        if (!this.singleInfoWindow) {
            this.singleInfoWindow = new google.maps.InfoWindow({content: contentString});
        } else {
            this.singleInfoWindow.setContent(contentString); 
        }
        this.singleInfoWindow.open(this.map, overlay);
	}

	this.addListener = function(event, callBack) {
		var self = this;
       
		google.maps.event.addListener(this.map, event, function() {
			var params = {};
			for ( var p = 0; p < arguments.length; p++) {
				if (arguments[p] != null) {
                    if (arguments[p].latLng != null)
                    {
                        params['lat'] = arguments[0].latLng.lat();
                        params['lng'] = arguments[0].latLng.lng();
                    }
				}
			}

			self.onEvent(callBack, params);
		});
	}

	this.addOverlayListener = function(overlayID, event) {
		var self = this;
		var overlay = this.overlays[overlayID];

		google.maps.event.addListener(overlay, event, function() {
			var params = {};
			for ( var p = 0; p < arguments.length; p++) {
				if (arguments[p] != null) {
					params['argument' + p] = arguments[p];
				}
			}

			params['overlay.latLng'] = overlay.getPosition();
			params['overlay.overlayId'] = overlay.overlayId;
			params['overlay.event'] = event;

			self.onEvent(self.overlayListenerCallbackUrl, params);
		});
	}

	this.clearOverlayListeners = function(overlayID, event) {
		var overlay = this.overlays[overlayID];

		google.maps.event.clearListeners(overlay, event);
	}

	this.setDraggingEnabled = function(enabled) {
		this.options.draggable= enabled;
		this.map.setOptions(this.options);
	}

	this.setDoubleClickZoomEnabled = function(enabled) {
		this.options.disableDoubleClickZoom = enabled;
		this.map.setOptions(this.options);
	}

	this.setScrollWheelZoomEnabled = function(enabled) {
        this.options.scrollwheel = enabled;
		this.map.setOptions(this.options);
	}

	this.setScaleControlEnabled = function(enabled) {
        this.options.scaleControl = enabled;
		this.map.setOptions(this.options);
	}
	
	this.setZoomControlEnabled = function(enabled) {
        this.options.zoomControl = enabled;
		this.map.setOptions(this.options);
	}
	
	this.setMapTypeControlEnabled = function(enabled) {
        this.options.mapTypeControl = enabled;
		this.map.setOptions(this.options);
	}

	this.setStreetViewControlEnabled = function(enabled) {
        this.options.streetViewControl = enabled;
		this.map.setOptions(this.options);
	}
	
	this.setPanControlEnabled = function(enabled) {
        this.options.panControl = enabled;
        this.map.setOptions(this.options);
    }

	this.getMapTypeString = function(mapType) {
		switch (mapType) {
		case google.maps.MapTypeId.ROADMAP:
			return 'ROADMAP';
			break;
		case google.maps.MapTypeId.SATELLITE:
			return 'SATELLITE';
			break;
		case google.maps.MapTypeId.HYBRID:
			return 'HYBRID';
			break;
		case google.maps.MapTypeId.TERRAIN:
			return 'TERRAIN';
			break;
		default:
			return 'unknown';
			break;
		}
	}
    
	this.setMapType = function(mapType) {
        this.map.setMapTypeId(mapType);
	}

	this.setZoom = function(level) {
		this.map.setZoom(level);
	}

	this.setCenter = function(center) {
		this.map.setCenter(center);
	}
	
	this.openInfoWindowHtml = function(latlng, myHtml) {
		this.map.openInfoWindowHtml(latlng, myHtml);
	}

	this.panTo = function(center) {
		this.map.panTo(center);
	}

	this.panDirection = function(dx, dy) {
        this.map.panBy(dx, dy);
	}

	this.zoomOut = function() {
	    this.map.setZoom(this.map.getZoom()-1)
	}

	this.zoomIn = function() {
		this.map.setZoom(this.map.getZoom()+1)
	}

	this.addOverlay = function(overlayId, overlay) {
		this.overlays[overlayId] = overlay;
		overlay.overlayId = overlayId;
		overlay.setMap(this.map);
		overlay.toString = function() {
			return overlayId;
		};
	}

	this.removeOverlay = function(overlayId) {
		if (this.overlays[overlayId] != null) {
			this.overlays[overlayId].setMap(null);
			this.overlays[overlayId] = null;
		}
	}

	this.clearOverlays = function() {
		if (this.overlays) {
		    for (i in this.overlays) {
		    	this.overlays[i].setMap(null);
		    }
		 }
		this.overlays = {};
	}
	
	this.triggerEvent = function(event) {
        google.maps.event.trigger(this.map, event);
    }

    this.triggerResize = function() {
        this.triggerEvent('resize');
    }

	this.openInfoWindow = function(latLng, contentMarkupId, marker) {
		var infoPanel = document.getElementById(contentMarkupId);
		if (this.infoWindow != null) {
			this.infoWindow.close();
		}
		if (latLng != null) {
			this.infoWindow = new google.maps.InfoWindow(
					{
						content: infoPanel, 
						position: latLng
					});			
		}
		else {
			this.infoWindow = new google.maps.InfoWindow(
					{
						content: infoPanel, 
					});			
		}
		if (marker != undefined && marker != null) {
			this.infoWindow.open(this.map, marker);
		}
		else {
			this.infoWindow.open(this.map);	
		}		
	}

	this.openMarkerInfoWindowTabs = function(markerId, tabs) {
		this.overlays[markerId].openInfoWindowTabs(tabs);
	}

	this.closeInfoWindow = function() {
		if (this.infoWindow != undefined) {
			this.infoWindow.close();
		}
	}	
}
