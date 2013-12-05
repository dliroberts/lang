package grammar.output;

import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.EdgePaintFunction;
import edu.uci.ics.jung.graph.decorators.EdgeShape;
import edu.uci.ics.jung.graph.decorators.EdgeShapeFunction;
import edu.uci.ics.jung.graph.decorators.EdgeStrokeFunction;
import edu.uci.ics.jung.graph.decorators.GlobalStringLabeller;
import edu.uci.ics.jung.graph.decorators.StringLabeller;
import edu.uci.ics.jung.graph.decorators.StringLabeller.UniqueLabelException;
import edu.uci.ics.jung.graph.decorators.ToolTipFunctionAdapter;
import edu.uci.ics.jung.graph.decorators.VertexFontFunction;
import edu.uci.ics.jung.graph.decorators.VertexPaintFunction;
import edu.uci.ics.jung.graph.decorators.VertexShapeFunction;
import edu.uci.ics.jung.graph.decorators.VertexStrokeFunction;
import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;
import edu.uci.ics.jung.graph.impl.DirectedSparseGraph;
import edu.uci.ics.jung.graph.impl.DirectedSparseVertex;
import edu.uci.ics.jung.visualization.BirdsEyeVisualizationViewer;
import edu.uci.ics.jung.visualization.FRLayout;
import edu.uci.ics.jung.visualization.Layout;
import edu.uci.ics.jung.visualization.PickedState;
import edu.uci.ics.jung.visualization.PluggableRenderer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.EditingModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;
import grammar.input.xml.DataManager;
import grammar.model.Form;
import grammar.model.Language;
import grammar.model.MatchType;
import grammar.model.PersonalPronounCategory;
import grammar.model.PersonalPronounRole;
import grammar.model.factory.ModelVerbFactory;
import grammar.model.verbs.ModelVerb;
import grammar.model.verbs.ModelVerb.ConjugatedVerb;
import grammar.model.verbs.Mood;
import grammar.model.verbs.Tense;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelVerbGraphViewer {
	private static final Logger LOGGER = LoggerFactory.getLogger(ModelVerbGraphViewer.class);

	private static Object o = DataManager.INSTANCES;
	private static Language language =
		Language.valueOf("FRENCH");
//		Language.valueOf("ENGLISH");
	private static StringLabeller vertexStringer;
	private static Map<ModelVerb, Vertex> vertices = new HashMap<ModelVerb, Vertex>();
	private static Map<Vertex, ModelVerb> modelVerbs = new HashMap<Vertex, ModelVerb>();
	private static Graph graph;
	private static VisualizationViewer vv;
	
	public static void main(String[] args) throws IOException {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e1) {
			// do nothing; inconsequential
		}
		
		final JFrame viewerDialog = new JFrame();
		graph = getGraph();
		PluggableRenderer pluggableRenderer = new PluggableRenderer();
		pluggableRenderer.setVertexStringer(vertexStringer);
		pluggableRenderer.setVertexPaintFunction(new HierarchicalVertexPaintFunction(modelVerbs));
		pluggableRenderer.setVertexShapeFunction(new HierarchicalVertexShapeFunction(modelVerbs));
		pluggableRenderer.setVertexFontFunction(new HierarchicalVertexFontFunction());
		pluggableRenderer.setVertexStrokeFunction(new HierarchicalVertexStrokeFunction());
		pluggableRenderer.setEdgeStrokeFunction(new HierarchicalEdgeStrokeFunction());
		pluggableRenderer.setEdgeShapeFunction(new HierarchicalEdgeShapeFunction());
		pluggableRenderer.setEdgePaintFunction(new HierarchicalEdgePaintFunction());
		
		final Layout layout = new FRLayout(graph);
        layout.initialize(new Dimension(0, 0));
        layout.resize(new Dimension(1800, 1800));
        
		vv = new VisualizationViewer(layout, pluggableRenderer);
		vv.setToolTipFunction(new ModelVerbSummaryToolTip(modelVerbs));
        
		final EditingModalGraphMouse graphMouse = new EditingModalGraphMouse() {
            public void mouseClicked(MouseEvent e) {
            	Set<Vertex> vertices = vv.getPickedState().getPickedVertices();
            	clearHighlighting();
            	for (Vertex vertex : vertices) {
            		highlightAncestorsOf(vertex);
            		highlightDescendantsOf(vertex);
            	}
            }
        };
		graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
        vv.setGraphMouse(graphMouse);
        
        final BirdsEyeVisualizationViewer bird =
            new BirdsEyeVisualizationViewer(vv, 0.25f, 0.25f);
        
        bird.initLens();

        Container c = viewerDialog.getContentPane();
        
        JComponent panelWest = new JPanel();
        panelWest.add(vv);
        panelWest.setBorder(new LineBorder(Color.gray));
        
        JComponent panelEast = new JPanel(new BorderLayout());
        panelEast.add(bird, BorderLayout.CENTER);
        panelEast.setBorder(new LineBorder(Color.gray));
        JPanel panelNorthEast = new JPanel();
        panelNorthEast.add(new JLabel("Find model for infinitive: "));
        final JTextField verbInputField = new JTextField(20);
        
        JButton conjugateButton = new JButton("Conjugate");
        conjugateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String infinitive = verbInputField.getText();
				
				try {
					ModelVerb matchingModelVerb = mvf.getModelVerb(infinitive.toLowerCase(), language);
					ConjugatedVerb conjugatedVerb = matchingModelVerb.getConjugatedVerb(infinitive);
					
					JDialog helpDialog = new JDialog(viewerDialog);
					helpDialog.setTitle("Verb Conjugation: "+infinitive);
					
					StringBuilder sb = new StringBuilder();
					sb.append(
							"<html><body>"+
							"<b>Model verb:</b> "+matchingModelVerb.toString()+"<br/>"+
							"<b>Auxiliary verb</b>: "+conjugatedVerb.getAuxiliary().toString()+"<br/>"
			        );
			        
					for (Mood m : Mood.values()) {
						boolean left = true;
						sb.append("<h1>"+m.toString().replaceAll("_", " ")+"</h1>");
						for (Tense t : m.getTenses()) {
							if (left)
								sb.append("<table><tr><td>");
							else
								sb.append("</td><td>");
							
							sb.append("<h2>"+t.toString().replaceAll("_", " ")+"</h2>"+
									"<table>");
							
							for (Form.FormCategory pc : conjugatedVerb.getModelVerb().getForms(t)) {
								sb.append("<tr><td>");
								sb.append(pc.toString().replaceAll("_", " "));
								sb.append("</td><td>");
								sb.append(conjugatedVerb.getForm(t, 
										(pc instanceof PersonalPronounCategory ?
												((PersonalPronounCategory) pc).getForms(PersonalPronounRole.SUBJECT) :
													pc.getAllForms())[0]
								));
								sb.append("</td></tr>");
							}
							
							if (conjugatedVerb.getModelVerb().getForms(t).length == 0) {
								sb.append("<tr><td>-</td></tr>");
							}
							
							sb.append("</table>");
							
							if (!left)
								sb.append("</td></tr></table>");
							
							left = !left;
						}
					}
					sb.append(("</body></html>"));
					helpDialog.getContentPane().add(new JScrollPane(new JLabel(sb.toString())));
			        
			        helpDialog.pack();
			        helpDialog.setVisible(true);
				}
				catch (IllegalArgumentException iae) {
					JOptionPane.showMessageDialog(viewerDialog,
							"No matching conjugation model found for infinitive '"+infinitive+"'.",
							"No Matching Verb Conjugation Model Found", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
        });
        
        JButton verbMatchButton = new JButton("Find");
        verbMatchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String infinitive = verbInputField.getText();
				
				try {
					ModelVerb matchingModelVerb = mvf.getModelVerb(infinitive.toLowerCase(), language);
					Vertex matchingVertex = vertices.get(matchingModelVerb);
					PickedState picked = vv.getPickedState();
					picked.clearPickedVertices();
					picked.clearPickedEdges();
					picked.pick(matchingVertex, true);
					
					MutableTransformer trans = vv.getViewTransformer();
					
					Point2D centre = vv.getCenter();
					double maxX = centre.getX()*2, maxY = centre.getY()*2;
					
					Point2D p = new Point2D.Double(trans.getTranslateX(), trans.getTranslateY());
					trans.setScale(1f, 1f, p);
					trans.setTranslate(
							maxX - (layout.getX(matchingVertex) * trans.getScaleX()) - (vv.getWidth()/2),
							maxY - (layout.getY(matchingVertex) * trans.getScaleY()) - (vv.getHeight()/2));
					graphMouse.mouseClicked(null);
				}
				catch (IllegalArgumentException iae) {
					JOptionPane.showMessageDialog(viewerDialog,
							"No matching conjugation model found for infinitive '"+infinitive+"'.",
							"No Matching Verb Conjugation Model Found", JOptionPane.ERROR_MESSAGE);
				}
			}
        });
        
        panelNorthEast.add(verbInputField);
        panelNorthEast.add(verbMatchButton);
        panelNorthEast.add(conjugateButton);
        panelEast.add(panelNorthEast, BorderLayout.NORTH);
        
        c.setLayout(new BorderLayout());
        c.add(panelWest, BorderLayout.CENTER);
        c.add(panelEast, BorderLayout.EAST);
        
        
		viewerDialog.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		viewerDialog.setTitle("Model Verb Hierarchy");
        viewerDialog.pack();
        viewerDialog.setVisible(true);
}
	
	private static void clearHighlighting() {
		ancestorEdges.clear();
		ancestorVertices.clear();
		descendantEdges.clear();
		descendantVertices.clear();
	}
	
	private static Set<Edge> ancestorEdges = new HashSet<Edge>();
	private static Set<Vertex> ancestorVertices = new HashSet<Vertex>();
	private static Set<Edge> descendantEdges = new HashSet<Edge>();
	private static Set<Vertex> descendantVertices = new HashSet<Vertex>();
	
	private static void highlightAncestorsOf(Vertex v) {
		Set<ModelVerb> mv = new HashSet<ModelVerb>();
		
		Queue<Vertex> unprocessedVerteces = new LinkedList<Vertex>();
		
		Vertex currentVertex = v;
		while (currentVertex != null) {
			ancestorVertices.add(currentVertex);
			mv.add(modelVerbs.get(currentVertex));
			
			Set<Edge> unprocessedEdges = currentVertex.getOutEdges();
			ancestorEdges.addAll(unprocessedEdges);
			for (Edge edge : unprocessedEdges) {
				unprocessedVerteces.offer(edge.getOpposite(currentVertex));
			}
			
			currentVertex = unprocessedVerteces.poll();
		}
	}
	
	private static void highlightDescendantsOf(Vertex v) {
		Set<ModelVerb> mv = new HashSet<ModelVerb>();
		
		Queue<Vertex> unprocessedVerteces = new LinkedList<Vertex>();
		
		Vertex currentVertex = v;
		while (currentVertex != null) {
			descendantVertices.add(currentVertex);
			mv.add(modelVerbs.get(currentVertex));
			
			Set<Edge> unprocessedEdges = currentVertex.getInEdges();
			
			if (LOGGER.isDebugEnabled()) {
				StringBuilder sb = new StringBuilder();
				sb.append("Direct dependants of "+mv.toString()+": [");
				for (Edge e : unprocessedEdges) {
					sb.append(modelVerbs.get(e.getOpposite(currentVertex))+", ");
				}
				sb.append("]");
				LOGGER.debug(sb.toString());
			}
			
			descendantEdges.addAll(unprocessedEdges);
			for (Edge edge : unprocessedEdges) {
				unprocessedVerteces.offer(edge.getOpposite(currentVertex));
			}
			
			currentVertex = unprocessedVerteces.poll();
		}
	}
	
	private static ModelVerbFactory mvf;

	private static Graph getGraph() throws IOException {
		DataManager.getInstance(language).load();
		mvf = ModelVerbFactory.getInstance();
		
		DirectedSparseGraph g = new DirectedSparseGraph();
		vertexStringer = GlobalStringLabeller.getLabeller(g);
		for (ModelVerb mv : ModelVerb.values()) {
			Vertex v = new DirectedSparseVertex();
			g.addVertex(v);
			vertices.put(mv, v);
			modelVerbs.put(v, mv);
			try {
				vertexStringer.setLabel(v, mv.getName());
			} catch (UniqueLabelException e) {
				throw new IllegalArgumentException("Duplicate verb name!");
			}
		}
		
		for (ModelVerb child : ModelVerb.values()) {
			List<ModelVerb> parents = child.getParents();
			for (ModelVerb parent : parents) {
				DirectedSparseEdge a2b =
					new DirectedSparseEdge(
							vertices.get(child),
							vertices.get(parent));
				g.addEdge(a2b);
			}
		}
		
		return g;
	}

	private static class HierarchicalVertexPaintFunction implements VertexPaintFunction {
		private Map<Vertex, ModelVerb> modelVerbs = new HashMap<Vertex, ModelVerb>();
		
		private HierarchicalVertexPaintFunction(Map<Vertex, ModelVerb> modelVerbs) {
			this.modelVerbs = modelVerbs;
		}
		
		public Paint getDrawPaint(Vertex v) {
			return Color.BLACK;
		}

		public Paint getFillPaint(Vertex v) {
			ModelVerb mv = modelVerbs.get(v);
			MatchType type = mv.getInfinitiveMatchers().size() == 0 ?
					MatchType.NONE :
					mv.getInfinitiveMatchers().get(0).getMatchType();
			
			switch (type) {
			case NONE: return Color.gray;
			case FULL_NAME: return Color.yellow;
			case SUFFIX: return Color.blue;
			case PATTERN: return new Color(200, 200, 255);
			case ALL: return Color.magenta;
			default: return Color.pink;
			}
		}
	}

	private static class ModelVerbSummaryToolTip extends ToolTipFunctionAdapter {
		private Map<Vertex, ModelVerb> modelVerbs = new HashMap<Vertex, ModelVerb>();
		
		public ModelVerbSummaryToolTip(Map<Vertex, ModelVerb> modelVerbs) {
			this.modelVerbs = modelVerbs;
		}
		
		public String getToolTipText(Vertex v) {
			ModelVerb mv = modelVerbs.get(v);
			return mv.getSummary();
		}
	}
	
	private static class HierarchicalVertexShapeFunction implements VertexShapeFunction {
		private Map<Vertex, ModelVerb> modelVerbs = new HashMap<Vertex, ModelVerb>();
		
		private HierarchicalVertexShapeFunction(Map<Vertex, ModelVerb> modelVerbs) {
			this.modelVerbs = modelVerbs;
		}

		public Shape getShape(Vertex v) {
			ModelVerb mv = modelVerbs.get(v);
			
			int sides = mv.getDepth() + 3; // can't render less than a triangle
			
			int radius;
			if (ancestorVertices.contains(v))
				radius = 25;
			else if (descendantVertices.contains(v))
				radius = 18;
			else
				radius = 10;
			
			int[] xPoints = new int[sides], yPoints = new int[sides];
			
			for (int side = 0; side < sides; side++) {
				double radianAngle = ((2d * Math.PI) / sides) * side;// + (((2d * Math.PI) / sides) / 2);
				xPoints[side] = (int) (Math.cos(radianAngle) * radius);
				yPoints[side] = (int) (Math.sin(radianAngle) * radius);
			}
			
			return new Polygon(xPoints, yPoints, sides);
		}
	}

	private static class HierarchicalEdgeStrokeFunction implements EdgeStrokeFunction {
		private final Stroke THIN = new BasicStroke(1);
		private final Stroke MEDIUM = new BasicStroke(2);
        private final Stroke THICK = new BasicStroke(4);
        
		public Stroke getStroke(Edge e) {
			if (ancestorEdges.contains(e))
				return THICK;
			else if (descendantEdges.contains(e))
				return MEDIUM;
			else
				return THIN;
		}
	}
	
	private static class HierarchicalEdgeShapeFunction implements EdgeShapeFunction {
		private final EdgeShapeFunction WEDGE = new EdgeShape.Wedge(30);
		private final EdgeShapeFunction LINE = new EdgeShape.Line();
		
		public Shape getShape(Edge e) {
			if (ancestorEdges.contains(e))
				return WEDGE.getShape(e);
			else if (descendantEdges.contains(e))
				return WEDGE.getShape(e);
			else
				return LINE.getShape(e);
		}
	}
	
	private static class HierarchicalEdgePaintFunction implements EdgePaintFunction {
		public Paint getDrawPaint(Edge e) {
			if (ancestorEdges.contains(e))
				return Color.orange;
			else if (descendantEdges.contains(e))
				return Color.BLUE;
			else
				return Color.black;
		}

		public Paint getFillPaint(Edge e) {
			if (ancestorEdges.contains(e))
				return Color.yellow;
			else if (descendantEdges.contains(e))
				return new Color(200, 200, 255);
			else
				return Color.black;
		}
	}
	
	private static class HierarchicalVertexFontFunction implements VertexFontFunction {
		public Font getFont(Vertex v) {
			if (ancestorVertices.contains(v))
				return new Font(null, Font.BOLD, 20);
			else if (descendantVertices.contains(v))
				return new Font(null, Font.BOLD|Font.ITALIC, 15);
			else
				return new Font(null, Font.PLAIN, 10);
		}
		
	}	
	
	private static class HierarchicalVertexStrokeFunction implements VertexStrokeFunction {
		private final Stroke THIN = new BasicStroke(1);
		private final Stroke THICK = new BasicStroke(4);
        
		public Stroke getStroke(Vertex v) {
			if (vv.getPickedState().getPickedVertices().contains(v))
				return THICK;
			else
				return THIN;
		}
		
	}	
}