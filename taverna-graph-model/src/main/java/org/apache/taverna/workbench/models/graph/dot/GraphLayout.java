package org.apache.taverna.workbench.models.graph.dot;
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

import static java.lang.Float.parseFloat;
import static org.apache.taverna.workbench.models.graph.Graph.Alignment.HORIZONTAL;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.taverna.workbench.models.graph.Graph;
import org.apache.taverna.workbench.models.graph.GraphController;
import org.apache.taverna.workbench.models.graph.GraphEdge;
import org.apache.taverna.workbench.models.graph.GraphElement;
import org.apache.taverna.workbench.models.graph.GraphNode;

import org.apache.log4j.Logger;

/**
 * Lays out a graph from a DOT layout.
 * 
 * @author David Withers
 */
public class GraphLayout implements DOTParserVisitor {
	private static final Logger logger = Logger.getLogger(GraphLayout.class);
	private static final int BORDER = 10;

	private Rectangle bounds;
	private Rectangle requiredBounds;
	private GraphController graphController;
	private int xOffset;
	private int yOffset;

	public Rectangle layoutGraph(GraphController graphController, Graph graph,
			String laidOutDot, Rectangle requiredBounds) throws ParseException {
		this.graphController = graphController;
		this.requiredBounds = requiredBounds;

		bounds = null;
		xOffset = 0;
		yOffset = 0;

		logger.debug(laidOutDot);
		DOTParser parser = new DOTParser(new StringReader(laidOutDot));
		parser.parse().jjtAccept(this, graph);

		// int xOffset = (bounds.width - bounds.width) / 2;
		// int yOffset = (bounds.height - bounds.height) / 2;

		return new Rectangle(xOffset, yOffset, bounds.width, bounds.height);
	}

	@Override
	public Object visit(SimpleNode node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTParse node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTGraph node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTStatementList node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTStatement node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTAttributeStatement node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTNodeStatement node, Object data) {
		GraphElement element = graphController.getElement(removeQuotes(node
				.getName()));
		if (element != null)
			return node.childrenAccept(this, element);
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTNodeId node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTPort node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTEdgeStatement node, Object data) {
		StringBuilder id = new StringBuilder();
		id.append(removeQuotes(node.getName()));
		if (node.getPort() != null) {
			id.append(":");
			id.append(removeQuotes(node.getPort()));
		}
		if (node.children != null)
			for (Node child : node.children)
				if (child instanceof ASTEdgeRHS) {
					NamedNode rhsNode = (NamedNode) child.jjtAccept(this, data);
					id.append("->");
					id.append(removeQuotes(rhsNode.getName()));
					if (rhsNode.getPort() != null) {
						id.append(":");
						id.append(removeQuotes(rhsNode.getPort()));
					}
				}
		GraphElement element = graphController.getElement(id.toString());
		if (element != null)
			return node.childrenAccept(this, element);
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTSubgraph node, Object data) {
		GraphElement element = graphController.getElement(removeQuotes(
				node.getName()).substring("cluster_".length()));
		if (element != null)
			return node.childrenAccept(this, element);
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTEdgeRHS node, Object data) {
		return node;
	}

	@Override
	public Object visit(ASTAttributeList node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTAList node, Object data) {
		if (data instanceof Graph) {
			Graph graph = (Graph) data;
			if ("bb".equalsIgnoreCase(node.getName())) {
				Rectangle rect = getRectangle(node.getValue());
				if (rect.width == 0 && rect.height == 0) {
					rect.width = 500;
					rect.height = 500;
				}
				if (bounds == null) {
					bounds = calculateBounds(rect);
					rect = bounds;
				}
				graph.setSize(rect.getSize());
				graph.setPosition(rect.getLocation());
			} else if ("lp".equalsIgnoreCase(node.getName())) {
				if (bounds != null)
					graph.setLabelPosition(getPoint(node.getValue()));
			}
		} else if (data instanceof GraphNode) {
			GraphNode graphNode = (GraphNode) data;
			if ("width".equalsIgnoreCase(node.getName()))
				graphNode.setSize(new Dimension(getSize(node.getValue()),
						graphNode.getHeight()));
			else if ("height".equalsIgnoreCase(node.getName()))
				graphNode.setSize(new Dimension(graphNode.getWidth(),
						getSize(node.getValue())));
			else if ("pos".equalsIgnoreCase(node.getName())) {
				Point position = getPoint(node.getValue());
				position.x = position.x - (graphNode.getWidth() / 2);
				position.y = position.y - (graphNode.getHeight() / 2);
				graphNode.setPosition(position);
			} else if ("rects".equalsIgnoreCase(node.getName())) {
				List<Rectangle> rectangles = getRectangles(node.getValue());
				List<GraphNode> sinkNodes = graphNode.getSinkNodes();
				if (graphController.getAlignment().equals(HORIZONTAL)) {
					Rectangle rect = rectangles.remove(0);
					graphNode.setSize(rect.getSize());
					graphNode.setPosition(rect.getLocation());
				} else {
					Rectangle rect = rectangles.remove(sinkNodes.size());
					graphNode.setSize(rect.getSize());
					graphNode.setPosition(rect.getLocation());
				}
				Point origin = graphNode.getPosition();
				for (GraphNode sinkNode : sinkNodes) {
					Rectangle rect = rectangles.remove(0);
					rect.setLocation(rect.x - origin.x, rect.y - origin.y);
					sinkNode.setSize(rect.getSize());
					sinkNode.setPosition(rect.getLocation());
				}
				for (GraphNode sourceNode : graphNode.getSourceNodes()) {
					Rectangle rect = rectangles.remove(0);
					rect.setLocation(rect.x - origin.x, rect.y - origin.y);
					sourceNode.setSize(rect.getSize());
					sourceNode.setPosition(rect.getLocation());
				}
			}
		} else if (data instanceof GraphEdge) {
			GraphEdge graphEdge = (GraphEdge) data;
			if ("pos".equalsIgnoreCase(node.getName()))
				graphEdge.setPath(getPath(node.getValue()));
		}
		return node.childrenAccept(this, data);
	}

	private Rectangle calculateBounds(Rectangle bounds) {
		bounds = new Rectangle(bounds);
		bounds.width += BORDER;
		bounds.height += BORDER;
		Rectangle newBounds = new Rectangle(bounds);
		double ratio = bounds.width / (float) bounds.height;
		double requiredRatio = requiredBounds.width
				/ (float) requiredBounds.height;
		// adjust the bounds so they match the aspect ration of the required bounds
		if (ratio > requiredRatio)
			newBounds.height = (int) (ratio / requiredRatio * bounds.height);
		else if (ratio < requiredRatio)
			newBounds.width = (int) (requiredRatio / ratio * bounds.width);

		xOffset = (newBounds.width - bounds.width) / 2;
		yOffset = (newBounds.height - bounds.height) / 2;
		// adjust the bounds and so they are not less than the required bounds
		if (newBounds.width < requiredBounds.width) {
			xOffset += (requiredBounds.width - newBounds.width) / 2;
			newBounds.width = requiredBounds.width;
		}
		if (newBounds.height < requiredBounds.height) {
			yOffset += (requiredBounds.height - newBounds.height) / 2;
			newBounds.height = requiredBounds.height;
		}
		// adjust the offset for the border
		xOffset += BORDER / 2;
		yOffset += BORDER / 2;
		return newBounds;
	}

	private List<Point> getPath(String value) {
		List<Point> path = new ArrayList<>();
		for (String point : removeQuotes(value).split(" ")) {
			String[] coords = point.split(",");
			if (coords.length == 2) {
				int x = (int) parseFloat(coords[0]) + xOffset;
				int y = (int) parseFloat(coords[1]) + yOffset;
				path.add(new Point(x, flipY(y)));
			}
		}
		return path;
	}

	private int flipY(int y) {
		return bounds.height - y;
	}

	private List<Rectangle> getRectangles(String value) {
		List<Rectangle> rectangles = new ArrayList<>();
		String[] rects = value.split(" ");
		for (String rectangle : rects)
			rectangles.add(getRectangle(rectangle));
		return rectangles;
	}

	private Rectangle getRectangle(String value) {
		String[] coords = removeQuotes(value).split(",");
		Rectangle rectangle = new Rectangle();
		rectangle.x = (int) parseFloat(coords[0]);
		rectangle.y = (int) parseFloat(coords[3]);
		rectangle.width = (int) parseFloat(coords[2]) - rectangle.x;
		rectangle.height = rectangle.y - (int) parseFloat(coords[1]);
		rectangle.x += xOffset;
		rectangle.y += yOffset;
		if (bounds != null)
			rectangle.y = flipY(rectangle.y);
		else
			rectangle.y = rectangle.height - rectangle.y;
		return rectangle;
	}

	private Point getPoint(String value) {
		String[] coords = removeQuotes(value).split(",");
		return new Point(xOffset + (int) parseFloat(coords[0]), flipY(yOffset
				+ (int) parseFloat(coords[1])));
	}

	private int getSize(String value) {
		return (int) (parseFloat(removeQuotes(value)) * 72);
	}

	private String removeQuotes(String value) {
		String result = value.trim();
		if (result.startsWith("\""))
			result = result.substring(1);
		if (result.endsWith("\""))
			result = result.substring(0, result.length() - 1);
		result = result.replaceAll("\\\\", "");
		return result;
	}
}
