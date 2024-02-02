package vineDiseases;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.SquareVertexStyle;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.ShapefileReader;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.awt.EventQueue;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class VineDiseaseDetectionPlugin extends AbstractPlugIn implements ActionListener {
	private PlugInContext plugInContext;

	@Override
	public boolean execute(PlugInContext context) throws Exception {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				actionPerformed(null);
			}
		});
		return true;
	}

	@Override
	public void initialize(PlugInContext context) throws Exception {
		this.plugInContext = context;
		// Add the plugin to the menu
		FeatureInstaller featureInstaller = context.getFeatureInstaller();
		featureInstaller.addMainMenuPlugin(this, new String[] { "Plugins", "Vine Disease Detection" }, "Load Layers",
				false, null, null);
	}

	private PlugInContext getPlugInContext() {
		return this.plugInContext;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		PlugInContext context = getPlugInContext();
		FeatureCollection reportFeatureCollection = null;
		FeatureCollection landingPadFeatureCollection = null;

		try {
			File boundaryLayerFile = promptForFile(context, "Select Boundary Layer File");
			if (boundaryLayerFile != null) {
				// Create DriverProperties and set the input file name
				DriverProperties boundaryDp = new DriverProperties();
				boundaryDp.set("File", boundaryLayerFile.getAbsolutePath());

				// Load the data from the shapefile
				ShapefileReader boundaryShapefileReader = new ShapefileReader();
				FeatureCollection boundaryFeatureCollection = boundaryShapefileReader.read(boundaryDp);

				// Create a FeatureSchema with date attribute
				FeatureSchema boundarySchema = new FeatureSchema();
				boundarySchema.addAttribute("geometry", AttributeType.GEOMETRY);
				boundarySchema.addAttribute("date", AttributeType.DATE);

				// Add features with the date attributes to the FeatureCollection
				FeatureCollection newBoundaryFeatureCollection = new FeatureDataset(
						boundaryFeatureCollection.getFeatureSchema());
				for (Feature boundaryFeature : boundaryFeatureCollection.getFeatures()) {
					Feature newBoundaryFeature = new BasicFeature(boundarySchema);
					newBoundaryFeature.setGeometry(boundaryFeature.getGeometry());
					if (boundaryFeature.getSchema().hasAttribute("date")) {
						newBoundaryFeature.setAttribute("date", boundaryFeature.getAttribute("date"));
					} else {
						newBoundaryFeature.setAttribute("date", null); // Set the default value to null
					}
					newBoundaryFeatureCollection.add(newBoundaryFeature);
				}

				// Create a layer with the specified name, FeatureCollection, and FeatureSchema
				Layer boundaryLayer = new Layer("Boundary Layer", Color.GREEN, newBoundaryFeatureCollection,
						context.getLayerManager());

				// Add the layer to the LayerManager
				context.getLayerManager().addLayer("Boundaries", boundaryLayer);
			}
			
			File extensionsLayerFile = promptForFile(context, "Select Extensions of the Vineyards' Layer File");
			if (extensionsLayerFile != null) {
				// Create DriverProperties and set the input file name
				DriverProperties extensionsDp = new DriverProperties();
				extensionsDp.set("File", extensionsLayerFile.getAbsolutePath());

				// Load the data from the shapefile
				ShapefileReader extensionsShapefileReader = new ShapefileReader();
				FeatureCollection extensionsFeatureCollection = extensionsShapefileReader.read(extensionsDp);

				// Create a FeatureSchema with date attribute
				FeatureSchema extensionsSchema = new FeatureSchema();
				extensionsSchema.addAttribute("geometry", AttributeType.GEOMETRY);
				extensionsSchema.addAttribute("date", AttributeType.DATE);

				// Add features with the date attributes to the FeatureCollection
				FeatureCollection newExtensionsFeatureCollection = new FeatureDataset(
						extensionsFeatureCollection.getFeatureSchema());
				for (Feature extensionsFeature : extensionsFeatureCollection.getFeatures()) {
					Feature newExtensionsFeature = new BasicFeature(extensionsSchema);
					newExtensionsFeature.setGeometry(extensionsFeature.getGeometry());
					if (extensionsFeature.getSchema().hasAttribute("date")) {
						newExtensionsFeature.setAttribute("date", extensionsFeature.getAttribute("date"));
					} else {
						newExtensionsFeature.setAttribute("date", null); // Set the default value to null
					}
					newExtensionsFeatureCollection.add(newExtensionsFeature);
				}

				// Create a layer with the specified name, FeatureCollection, and FeatureSchema
				Layer extensionsLayer = new Layer("Extensions Layer", Color.GREEN, newExtensionsFeatureCollection,
						context.getLayerManager());

				// Add the layer to the LayerManager
				context.getLayerManager().addLayer("Extensions", extensionsLayer);
			}

			File reportLayerFile = promptForFile(context, "Select Report Layer File");

			if (reportLayerFile != null) {
				// Create DriverProperties and set the input file name
				DriverProperties reportDp = new DriverProperties();
				reportDp.set("File", reportLayerFile.getAbsolutePath());

				// Load the data from the shapefile
				ShapefileReader reportShapefileReader = new ShapefileReader();
				reportFeatureCollection = reportShapefileReader.read(reportDp);

				// Create a FeatureSchema with date attribute
				FeatureSchema reportSchema = new FeatureSchema();
				reportSchema.addAttribute("geometry", AttributeType.GEOMETRY);
				reportSchema.addAttribute("date", AttributeType.DATE);

				// Add features with the date attributes to the FeatureCollection
				FeatureCollection newReportFeatureCollection = new FeatureDataset(
						reportFeatureCollection.getFeatureSchema());
				for (Feature reportFeature : reportFeatureCollection.getFeatures()) {
					Feature newReportFeature = new BasicFeature(reportSchema);
					newReportFeature.setGeometry(reportFeature.getGeometry());
					if (reportFeature.getSchema().hasAttribute("date")) {
						newReportFeature.setAttribute("date", reportFeature.getAttribute("date"));
					} else {
						newReportFeature.setAttribute("date", null);
					}
					newReportFeatureCollection.add(newReportFeature);
				}

				// Create a layer with the specified name, FeatureCollection, and FeatureSchema
				Layer reportLayer = new Layer("Report Layer", Color.BLUE, newReportFeatureCollection,
						context.getLayerManager());

				// Add the layer to the LayerManager
				context.getLayerManager().addLayer("Reports", reportLayer);
			}

			File landingPadLayerFile = promptForFile(context, "Select Landing Pad Layer File");
			if (landingPadLayerFile != null) {
				// Create DriverProperties and set the input file name
				DriverProperties landingPadDp = new DriverProperties();
				landingPadDp.set("File", landingPadLayerFile.getAbsolutePath());

				// Load the data from the shapefile
				ShapefileReader landingPadShapefileReader = new ShapefileReader();
				landingPadFeatureCollection = landingPadShapefileReader.read(landingPadDp);

				// Create a FeatureSchema with date attribute
				FeatureSchema landingPadSchema = new FeatureSchema();
				landingPadSchema.addAttribute("geometry", AttributeType.GEOMETRY);
				landingPadSchema.addAttribute("date", AttributeType.DATE);

				// Add features with the date attributes to the FeatureCollection
				FeatureCollection newLandingPadFeatureCollection = new FeatureDataset(
						landingPadFeatureCollection.getFeatureSchema());
				for (Feature landingPadFeature : landingPadFeatureCollection.getFeatures()) {
					Feature newLandingPadFeature = new BasicFeature(landingPadSchema);
					newLandingPadFeature.setGeometry(landingPadFeature.getGeometry());
					if (landingPadFeature.getSchema().hasAttribute("date")) {
						newLandingPadFeature.setAttribute("date", landingPadFeature.getAttribute("date"));
					} else {
						newLandingPadFeature.setAttribute("date", null);
					}
					newLandingPadFeatureCollection.add(newLandingPadFeature);
				}
				// Create a layer with the specified name, FeatureCollection, and FeatureSchema
				Layer landingPadLayer = new Layer("Landing Pad Layer", Color.BLUE, newLandingPadFeatureCollection,
						context.getLayerManager());

				// Add the layer to the LayerManager
				context.getLayerManager().addLayer("Landing Pads", landingPadLayer);

				// Customize the landing pad layer symbolization
				BasicStyle landingPadStyle = landingPadLayer.getBasicStyle();
				landingPadStyle.setRenderingFill(false); // Do not fill the shape
				landingPadStyle.setLineWidth(2); // Set the line width
				landingPadStyle.setLineColor(Color.BLUE); // Set the line color
				SquareVertexStyle landingPadVertexStyle = new SquareVertexStyle();
				landingPadVertexStyle.setEnabled(true); // Enable vertex style
				landingPadVertexStyle.setFillColor(Color.BLUE); // Set the fill color
				landingPadVertexStyle.setSize(12); // Set the size
				landingPadLayer.addStyle(landingPadVertexStyle); // Add the vertex style to the landing pad layer

				// Call computeFlightPaths after loading both layers
				if (reportFeatureCollection != null && landingPadFeatureCollection != null) {
					List<FeatureCollection> landingPadFeatureCollections = new ArrayList<>();
					landingPadFeatureCollections.add(landingPadFeatureCollection);

					List<Geometry> flightPaths = computeFlightPaths(context, reportFeatureCollection,
							landingPadFeatureCollection);

					// Create a FeatureSchema for flight paths
					FeatureSchema flightPathSchema = new FeatureSchema();
					flightPathSchema.addAttribute("geometry", AttributeType.GEOMETRY);
					FeatureCollection flightPathFeatureCollection = new FeatureDataset(flightPathSchema);

					// Add the flight path geometries to the flightPathFeatureCollection
					for (Geometry flightPath : flightPaths) {
						Feature flightPathFeature = new BasicFeature(flightPathSchema);
						flightPathFeature.setGeometry(flightPath);
						flightPathFeatureCollection.add(flightPathFeature);
					}

					// Create a layer for the flight paths
					Layer flightPathLayer = new Layer("Flight Paths", Color.RED, flightPathFeatureCollection,
							context.getLayerManager());

					// Add the layer to the LayerManager
					context.getLayerManager().addLayer("Flight Paths", flightPathLayer);
				}
			}
		} catch (Throwable t) {
			context.getWorkbenchContext().getErrorHandler().handleThrowable(t);
		}
	}

	// Prompt the user to select a file
	private File promptForFile(PlugInContext context, String title) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle(title);
		fileChooser.setMultiSelectionEnabled(false);

		// Add a file filter for specific shapefiles file type
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Shapefile (*.shp)", "shp");
		fileChooser.setFileFilter(filter);

		int returnVal = fileChooser.showOpenDialog(context.getWorkbenchFrame());
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		return fileChooser.getSelectedFile();
	}

	// Define a constant representing the maximum battery range for the drone.
	private static final double BATTERY_RANGE = 2000;

	// Initialize a list to store actions taken by the drone.
	List<String> droneActions = new ArrayList<>();

	// Define a constant for the output file path.
	private static final String OUTPUT_PATH = "C:\\geo\\Output\\";

	// Compute flight paths based on reports and landing pads.
	private List<Geometry> computeFlightPaths(PlugInContext context, FeatureCollection reportFeatureCollection,
			FeatureCollection landingPadFeatureCollection) {

		// Create an STRtree data structure for efficient spatial indexing.
		STRtree strTree = new STRtree();

		// Insert report features into the STRtree based on their envelopes.
		for (Feature report : reportFeatureCollection.getFeatures()) {
			Geometry geom = report.getGeometry();
			strTree.insert(geom.getEnvelopeInternal(), report);
		}

		strTree.build();

		// Initialize lists and variables to track drone actions and paths.
		List<Geometry> paths = new ArrayList<>();
		List<Feature> remainingReports = new ArrayList<>();
		GeometryFactory geometryFactory = new GeometryFactory();
		int reportNumber = 0;
		int pathsCreated = 0;
		double currentCoordToNextReport = 0;
		double nextReportToLandingPad = 0;

		// Loop through each landing pad.
		for (int i = 0; i < landingPadFeatureCollection.getFeatures().size(); i++) {

			// Initialize the remaining battery for this landing pad.
			double remainingBattery = BATTERY_RANGE;

			// Calculate the radius within which reports are reachable.
			final double radius = BATTERY_RANGE / 2;

			// Get the coordinate of the current landing pad.
			Coordinate landingPadCoord = landingPadFeatureCollection.getFeatures().get(i).getGeometry().getCoordinate();
			Coordinate currentCoord = landingPadCoord;

			droneActions.add("------------\nChecked landing pad " + i + " :" + landingPadCoord);

			// Find all reports within reach of the current landing pad.
			List<Feature> reportsWithinReach = getReportsWithinRadius(landingPadCoord, strTree, radius);

			// Initialize variables to track the current state of the drone's movement.
			remainingReports = reportsWithinReach;
			Feature nextReport = landingPadFeatureCollection.getFeatures().get(0);
			double totalDistanceTillNow = 0;

			// Loop to compute the flight paths.
			PathLoop: while (remainingReports.size() > 0) {
				boolean stop = false;
				// Find and draw paths.
				while (stop == false) {

					// Create a "final" copy of currentCoord, and remainingBattery
					final Coordinate currentCoordFinal = currentCoord;
					final double remainingBatteryFinal = remainingBattery;

					// Find the next report the drone should move to.
					nextReport = getNextReport(reportsWithinReach, currentCoordFinal, landingPadCoord,
							remainingBatteryFinal);

					if (nextReport == null) {
						if (remainingReports.isEmpty()) {// If there are no remaining reports left
							if (currentCoord != landingPadCoord) {// Create a path back to the landing pad if there are
																	// no more reachable reports.
								addPath(paths, currentCoord, landingPadCoord, geometryFactory);
								pathsCreated++;
								droneActions.add("A path got created!\n");
								stop = true;
								break PathLoop;
							}
						} else {
							nextReport = remainingReports.get(0);
						}
					}

					// Calculate distances
					currentCoordToNextReport = haversineDistance(currentCoord,
							nextReport.getGeometry().getCoordinate());
					nextReportToLandingPad = haversineDistance(nextReport.getGeometry().getCoordinate(),
							landingPadCoord);

					if (currentCoord == landingPadCoord) {
						// Handle drone starting from the landing pad.
						remainingBattery -= nextReportToLandingPad;
						totalDistanceTillNow += nextReportToLandingPad;
						addPath(paths, nextReport.getGeometry().getCoordinate(), landingPadCoord, geometryFactory);
						remainingReports.remove(nextReport);
						droneActions.add("**Moved from Landing pad to report: " + ++reportNumber + "**");
						droneActions.add("[Remaining battery: " + remainingBattery + "]");
						droneActions.add("[Total distance till now: " + totalDistanceTillNow + "]");
					} else {
						// Handle drone moving between reports.
						remainingBattery -= currentCoordToNextReport;
						totalDistanceTillNow += currentCoordToNextReport;

						// Check if it is possible to move to next report
						if (totalDistanceTillNow + nextReportToLandingPad < remainingBattery) {
							addPath(paths, currentCoord, nextReport.getGeometry().getCoordinate(), geometryFactory);
							remainingReports.remove(nextReport);
							droneActions.add("**Visited Report " + (++reportNumber) + ": "
									+ nextReport.getGeometry().getCoordinate().toString() + "**");
							droneActions.add("[Remaining battery: " + remainingBattery + "]");
							droneActions.add("[Total distance till now: " + totalDistanceTillNow + "]");
						} else {
							// Create a path back to the landing pad from current report if moving to the
							// next report is not possible.
							addPath(paths, currentCoord, landingPadCoord, geometryFactory);
							remainingBattery = BATTERY_RANGE;
							currentCoord = landingPadCoord;
							totalDistanceTillNow = 0;
							pathsCreated++;
							droneActions.add("A path got created!\n");
							droneActions.add("[Remaining battery: " + remainingBattery + "]");
							droneActions.add("[Total distance till now: " + totalDistanceTillNow + "]");
							stop = true;
							break;
						}
					}
					currentCoord = nextReport.getGeometry().getCoordinate();
				}
			}
		}

		droneActions.add("Returned to landing pad");
		droneActions.add("Number of Paths Created: " + pathsCreated);

		// Define the output file path for drone actions.
		String droneActionsFilePath = OUTPUT_PATH + "droneActions.txt";

		try {
			// Create the PrintWriter objects for each file.
			PrintWriter droneActionsWriter = new PrintWriter(Files.newBufferedWriter(Paths.get(droneActionsFilePath)));

			// Write the drone actions to the droneActions.txt file.
			droneActionsWriter.println("Drone's actions:");
			droneActions.forEach(droneActionsWriter::println);

			// Close the PrintWriter objects to flush any remaining data in the buffer and
			// release the file resources.
			droneActionsWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return paths;
	}

	// Method to get reports within a specified radius of the landing pad.
	List<Feature> getReportsWithinRadius(Coordinate landingPadCoord, STRtree strTree, double radius) {
		List<Feature> reportsWithinReach = new ArrayList<>();

		// Create a point representing the landing pad's location.
		GeometryFactory geometryFactory = new GeometryFactory();
		Point landingPadPoint = geometryFactory.createPoint(landingPadCoord);

		// Create a circular buffer around the landing pad.
		Geometry buffer = landingPadPoint.buffer(radius);

		// Query the STRtree for features within the buffer.
		@SuppressWarnings("unchecked")
		List<Feature> nearbyFeatures = strTree.query(buffer.getEnvelopeInternal());

		// Check if each nearby feature is within the specified radius.
		for (Feature report : nearbyFeatures) {
			Coordinate reportCoord = report.getGeometry().getCoordinate();
			double distance = haversineDistance(landingPadCoord, reportCoord);
			if (distance <= radius) {
				reportsWithinReach.add(report);
			}
		}
		return reportsWithinReach;
	}

	// Method to find the next report the drone should move to.
	private Feature getNextReport(List<Feature> reports, Coordinate currentCoord, Coordinate landingPadCoord,
			double remainingBattery) {
		return reports.stream()
				.filter(report -> isWithinBatteryRange(report, currentCoord, landingPadCoord, remainingBattery))
				.min(Comparator.comparingDouble(report -> getTotalDistance(report, currentCoord, landingPadCoord)))
				.orElse(null);
	}

	// Method to calculate the total distance from the current position to a report
	// and back to the landing pad.
	private double getTotalDistance(Feature report, Coordinate currentCoord, Coordinate landingPadCoord) {
		Coordinate reportCoord = report.getGeometry().getCoordinate();
		double distanceToReport = haversineDistance(currentCoord, reportCoord);
		double distanceReportToLandingPad = haversineDistance(reportCoord, landingPadCoord);

		return distanceToReport + distanceReportToLandingPad;
	}

	// Method to check if a report is within the drone's remaining battery range.
	private boolean isWithinBatteryRange(Feature report, Coordinate currentCoord, Coordinate landingPadCoord,
			double remainingBattery) {
		Coordinate reportCoord = report.getGeometry().getCoordinate();
		double totalDistance;

		if (currentCoord == landingPadCoord) {
			totalDistance = haversineDistance(reportCoord, landingPadCoord);
		} else {
			double distanceToReport = haversineDistance(currentCoord, reportCoord);
			double distanceReportToLandingPad = haversineDistance(reportCoord, landingPadCoord);
			totalDistance = distanceToReport + distanceReportToLandingPad;
		}

		return totalDistance <= remainingBattery;
	}

	// Method to add a path to the list of flight paths.
	private void addPath(List<Geometry> paths, Coordinate fromCoord, Coordinate toCoord,
			GeometryFactory geometryFactory) {
		Coordinate[] pathCoords = new Coordinate[] { fromCoord, toCoord };
		Geometry path = geometryFactory.createLineString(pathCoords);
		paths.add(path);
	}

	// Method to calculate the Haversine distance between two coordinates.
	public static double haversineDistance(Coordinate coord1, Coordinate coord2) {
		// convert latitude and longitude values from degrees to radians
		double lat1 = Math.toRadians(coord1.y);
		double lon1 = Math.toRadians(coord1.x);
		double lat2 = Math.toRadians(coord2.y);
		double lon2 = Math.toRadians(coord2.x);

		// calculate differences between latitudes and longitudes
		double dLat = lat2 - lat1;
		double dLon = lon2 - lon1;

		// apply the Haversine formula
		double a = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dLon / 2), 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		// Earth's mean radius in meters
		final double EARTH_RADIUS = 6371000;

		// calculate and return the distance in meters
		return EARTH_RADIUS * c;
	}
}
