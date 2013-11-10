package sk.seges.sesam.core.pap.model.api;

public enum PropagationType {
	PROPAGATED_MUTABLE, //defined from outside of the class using appropriate setter method
	PROPAGATED_IMUTABLE, //define from outside of the class using constructor
	INSTANTIATED; //instantiated from inside of the class - when/where needed
}
