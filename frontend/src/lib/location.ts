/**
 * Single source of truth for the restaurant's real-world location.
 * Update the address/coordinates here — every map, link, and directions
 * button on the site reads from this file.
 */
export const restaurantLocation = {
  name: "Twisted Momos",
  address:
    "512, KRS Complex, K R Puram Police Station Road, Near Uttam Medicals, Ramamurthy Nagar, Bengaluru, Karnataka",
  // Kept for internal accuracy (e.g. distance/geo logic) — Maps URLs below use
  // the name + address instead, so links resolve to a readable place, not raw coordinates.
  coords: {
    lat: 13.0119098,
    lng: 77.6680331,
  },
};

/** Human-readable "name, address" query used to build every Google Maps URL below. */
const destinationQuery = encodeURIComponent(`${restaurantLocation.name}, ${restaurantLocation.address}`);

/** No-API-key embeddable Google Maps iframe source, centered on the location. */
export const googleMapsEmbedSrc = `https://www.google.com/maps?q=${destinationQuery}&z=16&output=embed`;

/** Opens the location itself (with marker) in Google Maps, labelled by name/address. */
export const googleMapsViewUrl = `https://www.google.com/maps/search/?api=1&query=${destinationQuery}`;

/** Opens turn-by-turn navigation to the location in Google Maps, labelled by name/address. */
export const googleMapsDirectionsUrl = `https://www.google.com/maps/dir/?api=1&destination=${destinationQuery}`;
