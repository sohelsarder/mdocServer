package utils;

import java.io.File;

import play.Play;

public class ModelGenerator {

	String fileName;
	
	String packageName;
	String importNames;
	String classDefinition;
	
	String [] fields;
	
	String [] functions;
	
	String end;
	
	public ModelGenerator(){}
	
	private void GenerateModel(){
		String pathname = Play.applicationPath.getAbsolutePath()+"/app/models/";
		File f = new File(pathname+fileName);
	}
	
}
