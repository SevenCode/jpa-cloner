package sk.nociar.jpacloner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.HashSet;
import java.util.Set;

import sk.nociar.jpacloner.entities.Bar;
import sk.nociar.jpacloner.entities.BaseEntity;
import sk.nociar.jpacloner.entities.Baz;
import sk.nociar.jpacloner.entities.Edge;
import sk.nociar.jpacloner.entities.Foo;
import sk.nociar.jpacloner.entities.Node;
import sk.nociar.jpacloner.entities.Point;
import sk.nociar.jpacloner.graphs.PropertyFilter;

public abstract class JpaClonerTestSupport {
	private Node n1;

	private void addChildren(Node parent, Node... children ) {
		for (int i = 0; i < children.length; i++) {
			Node child = children[i];
			Edge edge = createEdge();
			edge.setParent(parent);
			edge.setChild(child);
			edge.setPosition(i + 1);
			// add to nodes
			parent.getChildren().put(edge.getPosition(), edge);
			child.getParents().add(edge);
		}
	}
	
	public void initialize() {
		// initialize foo, baz, bar
		Foo foo1 = createFoo();
		Foo foo2 = createFoo();
		Baz baz1 = createBaz();
		Baz baz2 = createBaz();
		Bar bar = createBar();
		foo1.setBar(bar);
		foo2.setBar(bar);
		baz1.setBar(bar);
		baz2.setBar(bar);
		// initialize nodes & edges
		n1 = createNode("1", 1, 2);
		Node n1_1 = createNode("1.1", 3, 4);
		Node n1_1_1 = createNode("1.1.1", 5, 6);
		Node n1_1_2 = createNode("1.1.2", 7, 8);
		Node n1_1_3 = createNode("1.1.3", 9, 10);
		Node n1_2 = createNode("1.2", 11, 12);
		Node n1_2_1 = createNode("1.2.1", 13, 14);
		Node n1_2_2 = createNode("1.2.2", 15, 16);
		Node n1_2_3 = createNode("1.2.3", 17, 18);
		n1_1.setBaz(baz1);
		n1_1_1.setBaz(baz2);
		n1_1_2.setBaz(baz1);
		n1_1_3.setBaz(baz2);
		n1_2.setBaz(baz1);
		n1_2_1.setBaz(baz2);
		n1_2_2.setBaz(baz1);
		n1_2_3.setBaz(baz2);
		n1_1.setFoo(foo1);
		n1_1_1.setFoo(foo2);
		n1_1_2.setFoo(foo1);
		n1_1_3.setFoo(foo2);
		n1_2.setFoo(foo1);
		n1_2_1.setFoo(foo2);
		n1_2_2.setFoo(foo1);
		n1_2_3.setFoo(foo2);
		// setup parent-child relationship
		addChildren(n1, n1_1, n1_2);
		addChildren(n1_1, n1_1_1, n1_1_2, n1_1_3);
		addChildren(n1_2, n1_2_1, n1_2_2, n1_2_3);
		// add cycles
		addChildren(n1_1_1, n1);
		addChildren(n1_2_3, n1);
	}
	
	/**
	 * Helper method for assertions.
	 */
	private void assertCloned(JpaExplorer jpaExplorer, Class<?> clazz, int expected) {
		int count = jpaExplorer.getEntities(clazz).size();
		assertEquals(expected, count);
	}
	
	private void assertNode(Node original, Node clone) {
		assertEquals(original, clone);
		assertEquals(original.getName(), clone.getName());
		assertEquals(original.getBaz(), clone.getBaz());
		assertEquals(original.getFoo(), clone.getFoo());
		
		assertNotSame(original, clone);
		if (original.getBaz() != null) {
			assertNotSame(original.getBaz(), clone.getBaz());
			assertEquals(original.getBaz().getBar(), clone.getBaz().getBar());
			if (original.getBaz().getBar() != null) {
				assertNotSame(original.getBaz().getBar(), clone.getBaz().getBar());
			}
		}
		if (original.getFoo() != null) {
			assertNotSame(original.getFoo(), clone.getFoo());
			assertEquals(original.getFoo().getBar(), clone.getFoo().getBar());
			if (original.getFoo().getBar() != null) {
				assertNotSame(original.getFoo().getBar(), clone.getFoo().getBar());
			}
		}
		// point
		assertNotSame(original.getPoint(), clone.getPoint());
		assertEquals(original.getPoint().getX(), clone.getPoint().getX()); 
		assertEquals(original.getPoint().getY(), clone.getPoint().getY());
		// child edges
		for (Integer position : original.getChildren().keySet()) {
			Edge originalEdge = original.getChildren().get(position);
			Edge clonedEdge = clone.getChildren().get(position);
			assertEquals(originalEdge, clonedEdge);
			assertNotSame(originalEdge, clonedEdge);
			assertSame(clone, clonedEdge.getParent());
		}
	}

	private static final PropertyFilter defaultPropertyFilter = new PropertyFilter() {
		@Override
		public boolean test(Object entity, String property) {
			return true;
		}
	};

	public void testClone1() {
		Node o1 = getOriginal();
		Node o1_1 = o1.getChildren().get(1).getChild();
		Node o1_1_1 = o1_1.getChildren().get(1).getChild();
		Node o1_1_2 = o1_1.getChildren().get(2).getChild();
		Node o1_1_3 = o1_1.getChildren().get(3).getChild();
		Node o1_2 = o1.getChildren().get(2).getChild();
		Node o1_2_1 = o1_2.getChildren().get(1).getChild();
		Node o1_2_2 = o1_2.getChildren().get(2).getChild();
		Node o1_2_3 = o1_2.getChildren().get(3).getChild();
		
		Node c1 = JpaCloner.clone(o1, "(children.value.child)*.(point|(foo|baz).bar.dummy_property)");
		JpaExplorer jpaExplorer = JpaExplorer.doExplore(c1, defaultPropertyFilter);
		// asserts counts
		assertCloned(jpaExplorer, Node.class, 9);
		assertCloned(jpaExplorer, Edge.class, 10);
		assertCloned(jpaExplorer, Point.class, 9);
		assertCloned(jpaExplorer, Foo.class, 2);
		assertCloned(jpaExplorer, Baz.class, 2);
		assertCloned(jpaExplorer, Bar.class, 1);
		// assert object clones
		Node c1_1 = c1.getChildren().get(1).getChild();
		Node c1_1_1 = c1_1.getChildren().get(1).getChild();
		Node c1_1_1_1 = c1_1_1.getChildren().get(1).getChild();
		Node c1_1_2 = c1_1.getChildren().get(2).getChild();
		Node c1_1_3 = c1_1.getChildren().get(3).getChild();
		Node c1_2 = c1.getChildren().get(2).getChild();
		Node c1_2_1 = c1_2.getChildren().get(1).getChild();
		Node c1_2_2 = c1_2.getChildren().get(2).getChild();
		Node c1_2_3 = c1_2.getChildren().get(3).getChild();
		Node c1_2_3_1 = c1_2_3.getChildren().get(1).getChild();
		
		assertNode(o1, c1);
		assertNode(o1_1, c1_1);
		assertNode(o1_1_1, c1_1_1);
		assertNode(o1_1_2, c1_1_2);
		assertNode(o1_1_3, c1_1_3);
		assertNode(o1_2, c1_2);
		assertNode(o1_2_1, c1_2_1);
		assertNode(o1_2_2, c1_2_2);
		assertNode(o1_2_3, c1_2_3);
		
		assertSame(c1, c1_1_1_1);
		assertSame(c1, c1_2_3_1);
		
		assertSame(c1_1.getBaz(), c1_2.getBaz());
		assertSame(c1_1_1.getBaz(), c1_2_1.getBaz());
		assertSame(c1_1_2.getBaz(), c1_2_2.getBaz());
		assertSame(c1_1_3.getBaz(), c1_2_3.getBaz());
		assertSame(c1_1_1.getBaz(), c1_1_3.getBaz());
	}

	public void testClone2() {
		Node clone = JpaCloner.clone(getOriginal(), "(children.value.child)*.(foo|baz)");
		JpaExplorer jpaExplorer = JpaExplorer.doExplore(clone, defaultPropertyFilter);
		// do some asserts
		assertCloned(jpaExplorer, Node.class, 9);
		assertCloned(jpaExplorer, Edge.class, 10);
		assertCloned(jpaExplorer, Point.class, 0);
		assertCloned(jpaExplorer, Foo.class, 2);
		assertCloned(jpaExplorer, Baz.class, 2);
		assertCloned(jpaExplorer, Bar.class, 0);
	}

	public void testClone3() {
		Node clone = JpaCloner.clone(getOriginal(), "(children.value.child)*.(foo.bar|baz.bar)");
		JpaExplorer jpaExplorer = JpaExplorer.doExplore(clone, defaultPropertyFilter);
		// do some asserts
		assertCloned(jpaExplorer, Node.class, 9);
		assertCloned(jpaExplorer, Edge.class, 10);
		assertCloned(jpaExplorer, Point.class, 0);
		assertCloned(jpaExplorer, Foo.class, 2);
		assertCloned(jpaExplorer, Baz.class, 2);
		assertCloned(jpaExplorer, Bar.class, 1);
	}

	public void testClone4() {
		Node clone = JpaCloner.clone(getOriginal(), "(parents.parent)*|(children.value.child)*.(baz$.bar)");
		JpaExplorer jpaExplorer = JpaExplorer.doExplore(clone, defaultPropertyFilter);
		// do some asserts
		assertCloned(jpaExplorer, Node.class, 9);
		assertCloned(jpaExplorer, Edge.class, 10);
		assertCloned(jpaExplorer, Point.class, 0);
		assertCloned(jpaExplorer, Foo.class, 0);
		assertCloned(jpaExplorer, Baz.class, 2);
		assertCloned(jpaExplorer, Bar.class, 0);
		
		assertParents(clone, new HashSet<Edge>());
	}
	
	public void testClone5() {
		PropertyFilter filter = new PropertyFilter() {
			@Override
			public boolean test(Object entity, String property) {
				return !"id".equals(property);
			}
		};
		Node clone = JpaCloner.clone(getOriginal(), filter, "(children.value.child)*.(foo|baz).bar");
		JpaExplorer jpaExplorer = JpaExplorer.doExplore(clone, defaultPropertyFilter);
		// do some asserts
		assertCloned(jpaExplorer, Node.class, 9);
		assertCloned(jpaExplorer, Edge.class, 10);
		assertCloned(jpaExplorer, Point.class, 0);
		assertCloned(jpaExplorer, Foo.class, 2);
		assertCloned(jpaExplorer, Baz.class, 2);
		assertCloned(jpaExplorer, Bar.class, 1);
		// assert that each cloned object has null id
		for (BaseEntity entity : jpaExplorer.getEntities(BaseEntity.class)) {
			assertNull(entity.getId());
		}		
	}
	
	public void testExplore() {
		JpaExplorer jpaExplorer = JpaExplorer.doExplore(getOriginal(), defaultPropertyFilter);
		// do some asserts
		assertCloned(jpaExplorer, Node.class, 9);
		assertCloned(jpaExplorer, Edge.class, 10);
		assertCloned(jpaExplorer, Point.class, 9);
		assertCloned(jpaExplorer, Foo.class, 2);
		assertCloned(jpaExplorer, Baz.class, 2);
		assertCloned(jpaExplorer, Bar.class, 1);
		
		jpaExplorer = JpaExplorer.doExplore(getOriginal(), new PropertyFilter() {
			@Override
			public boolean test(Object entity, String property) {
				return !"point".equals(property) && !"bar".equals(property);
			}
		});
		
		// do some asserts
		assertCloned(jpaExplorer, Node.class, 9);
		assertCloned(jpaExplorer, Edge.class, 10);
		assertCloned(jpaExplorer, Point.class, 0);
		assertCloned(jpaExplorer, Foo.class, 2);
		assertCloned(jpaExplorer, Baz.class, 2);
		assertCloned(jpaExplorer, Bar.class, 0);
		
		jpaExplorer = JpaExplorer.doExplore(getOriginal(), "(children.value.child)*.(foo|baz)");
		
		// do some asserts
		assertCloned(jpaExplorer, Node.class, 9);
		assertCloned(jpaExplorer, Edge.class, 10);
		assertCloned(jpaExplorer, Point.class, 0);
		assertCloned(jpaExplorer, Foo.class, 2);
		assertCloned(jpaExplorer, Baz.class, 2);
		assertCloned(jpaExplorer, Bar.class, 0);
		
		jpaExplorer = JpaExplorer.doExplore(getOriginal());
		
		// do some asserts
		assertCloned(jpaExplorer, Node.class, 1);
		assertCloned(jpaExplorer, Edge.class, 0);
		assertCloned(jpaExplorer, Point.class, 0);
		assertCloned(jpaExplorer, Foo.class, 0);
		assertCloned(jpaExplorer, Baz.class, 0);
		assertCloned(jpaExplorer, Bar.class, 0);
	}
	
	private void assertParents(Node node, Set<Edge> asserted) {
		for (Edge edge : node.getParents()) {
			if (!asserted.contains(edge)) {
				asserted.add(edge);
				assertSame(node, edge.getChild());
				assertParents(edge.getParent(), asserted);
			}
		}
	}
	
	private Node createNode(String name, int x, int y) {
		Node node = createNode();
		node.setName(name);
		node.setPoint(new Point(x, y));
		return node;
	}
	
	protected Node getOriginal() {
		return n1;
	}
	
	protected abstract Node createNode();
	protected abstract Edge createEdge();
	protected abstract Point createPoint();
	protected abstract Foo createFoo();
	protected abstract Bar createBar();
	protected abstract Baz createBaz();
	
}
