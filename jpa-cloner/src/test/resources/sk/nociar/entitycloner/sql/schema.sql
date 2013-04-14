CREATE TABLE Bar(
	id INTEGER IDENTITY, 
	PRIMARY KEY (id)
);

CREATE TABLE Baz(
	id INTEGER IDENTITY, 
	bar_id INTEGER, 
	PRIMARY KEY (id),
	FOREIGN KEY (bar_id) REFERENCES Bar(id)
);

CREATE TABLE Foo(
	id INTEGER IDENTITY, 
	bar_id INTEGER, 
	PRIMARY KEY (id),
	FOREIGN KEY (bar_id) REFERENCES Bar(id)
);

CREATE TABLE Node(
	id INTEGER IDENTITY, 
	name VARCHAR(255) NOT NULL, 
	x INTEGER NOT NULL, 
	y INTEGER NOT NULL,
	foo_id INTEGER,
	baz_id INTEGER,
	PRIMARY KEY (id),
	FOREIGN KEY (foo_id) REFERENCES Foo(id),
	FOREIGN KEY (baz_id) REFERENCES Baz(id)
);

CREATE TABLE Edge(
	id INTEGER IDENTITY, 
	pos INTEGER NOT NULL, 
	parent_id INTEGER NOT NULL,
	child_id INTEGER NOT NULL,
	bar_id INTEGER, 
	PRIMARY KEY (id), 
	FOREIGN KEY (parent_id) REFERENCES Node(id),
	FOREIGN KEY (child_id) REFERENCES Node(id),
	FOREIGN KEY (bar_id) REFERENCES Bar(id)
);