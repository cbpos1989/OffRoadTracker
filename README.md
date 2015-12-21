# OffRoad Tracker <h1> 
An app that will allow the users to track their hikes or bike rides. The key to the app usefulness is the fact that the user will download the map tiles to their phone before heading out so that they can navigate and track their route with the need of data or wifi. Primarily focused towards mountain hikers and off-road bikers who need maps for navigation,but not at the cost of their phone battery or data.

## OffRoad Tracker Plan <h2>
## Implemented Features: <h2>
###User GPS Location
*Displays current user's position.
*Draw polylines between current position and previous position.
*Zooms to user's current position.
*User can pause route and end route.
###Live route tracking with lines marking the route the user has taken.
*User can switch back and forth between Main Menu and Map with the route persisting.
*Save current route to internal GPX file.
###Live Compass
*Main menu will feature a live updating compass.
*Orientation style compass for increased navigation. (Done but could user better image)
###Main Menu
*Live Compass.
*Button to open map.
*Coordinates field for the user to define a starting location. (Needs Better Error Handling)
###Custom user markers
*User can place route markers on map to leave advice/guidance for others.
*Persistence on the map using Firebase to store markers to database.
###Load Previous Route
*Can load previous gpx file and automatically start plotting lines to mimic real life tracking of a route.

## Planned Features: <h2>
###Live route tracking with lines marking the route the user has taken.
*User can save route after hike/ride.
*Allow user to export gpx file after ride.
*Routes will remain on the map for other user to view later.
*Differentiation between hikes and bike rides (Simple Color Change)
###Custom user markers
*Make route markers persistent (possibly on server)
*Various different types of images for different types of markers (Views, hazards, shortcut, etc.)
*Only draw markers that are near the user’s location.
###Live Compass
*Compass will feature on main map screen and will be clickable to bring up a larger version.
*Main Menu
*Button to Download Map
###Download Map
*Allow user to enter coordinates and zoom level to download map tile.
*User can download as many tiles as needed. (not sure if compliant with T&C of Google API)
###User Details
*Allow user to setup up user profile
*Profile Image
*Name
*Biker/Hiker
*Username appears on custom markers
