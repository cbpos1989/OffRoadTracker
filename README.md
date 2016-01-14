# OffRoad Tracker <h1> 
ORT will help mountain bikers and hikers navigate when traversing of the beaten track. With the help of Google Maps and a few extra features of the app, anyone who likes to adventure in the great outdoors should be able to navigate more efficiently. On play store as closed alpha https://play.google.com/store/apps/details?id=cbpos1989.com.offroadtracker.

## OffRoad Tracker Plan <h2>
### Implemented Features: <h2>
####User GPS Location
-User can save route after hike/ride.
-Allow user to export gpx file after ride.
-Displays current user's position.
-Draw polylines between current position and previous position.
-Zooms to user's current position.
-User can pause route and end route.
####Live route tracking with lines marking the route the user has taken.
-User can switch back and forth between Main Menu and Map with the route persisting.
-Save current route to internal GPX file.
####Live Compass
-Main menu will feature a live updating compass.
-Orientation style compass for increased navigation. (Done but could user better image)
####Main Menu
-Live Compass.
-Button to open map.
-Coordinates field for the user to define a starting location. (Needs Better Error Handling)
####Custom user markers
-User can place route markers on map to leave advice/guidance for others.
-Persistence on the map using Firebase to store markers to database.
-Various different types of images for different types of markers (Views, hazards, shortcut, etc.)
####Load Previous Route
-Can load previous gpx file and automatically start plotting lines to mimic real life tracking of a route.
-Three different speeds to playback.

### Planned Features: <h2>
####Live route tracking with lines marking the route the user has taken.
-Routes will remain on the map for other user to view later.
-Differentiation between hikes and bike rides (Simple Color Change)
####Custom user markers
-Only draw markers that are near the user’s location.
####Live Compass
-Compass will feature on main map screen and will be clickable to bring up a larger version.
####Download Map
-Allow user to enter coordinates and zoom level to download map tile.
-User can download as many tiles as needed. (not sure if compliant with T&C of Google API)
####User Details
-Allow user to setup up user profile
-Profile Image
-Name
-Biker/Hiker
-Username appears on custom markers
####Load Previous Route
-Give option for the user to fast forward to a particular point on a route. Using a slider.
