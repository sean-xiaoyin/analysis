package edu.utsa.fileflow.client.fileflow;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import edu.utsa.fileflow.testutils.GraphvizGenerator;

public class FileStructureTest {

	private FileStructure fs;

	@Before
	public void setUp() throws Exception {
		Automaton.setMinimization(Automaton.MINIMIZE_BRZOZOWSKI);
		Automaton.setMinimizeAlways(true);
		fs = new FileStructure();
	}

	@Test
	public void testDirectoryExists() throws FileStructureException {
		fs.createDirectory(new VariableAutomaton("/home"));
		// '/' exists by default since it is root
		assertTrue("'/' should exist", fs.fileExists(new VariableAutomaton("/")));
		assertTrue("'/home' should exist", fs.fileExists(new VariableAutomaton("/home")));
		assertFalse("'/fake' should not exist", fs.fileExists(new VariableAutomaton("/fake")));
	}

	@Test
	public void testFileExists() throws FileStructureException {
		fs.createDirectory(new VariableAutomaton("/home"));
		fs.createFile(new VariableAutomaton("/home/file"));

		// '/' exists by default since it is root
		assertTrue("'/' should exist", fs.fileExists(new VariableAutomaton("/")));
		assertTrue("'/home' should exist", fs.fileExists(new VariableAutomaton("/home")));
		assertTrue("'/home/file' should exist", fs.fileExists(new VariableAutomaton("/home/file")));
		assertFalse("'/fake' should not exist", fs.fileExists(new VariableAutomaton("/fake")));
	}

	@Test(expected = FileStructureException.class)
	public void testCreateFileWhenDirectoryAlreadyExists() throws FileStructureException {
		// create and assert '/a/'
		fs.createDirectory(new VariableAutomaton("/a/"));
		assertTrue(fs.fileExists(new VariableAutomaton("/a/")));

		// attempt to create file at existing directory '/a' (should fail)
		fs.createFile(new VariableAutomaton("/a"));
	}

	@Test(expected = FileStructureException.class)
	public void testCreateDirectoryWhenFileAlreadyExists() throws FileStructureException {
		// create and assert '/a'
		fs.createFile(new VariableAutomaton("/a"));
		assertTrue(fs.fileExists(new VariableAutomaton("/a")));

		// attempt to create directory at existing file '/a' (should fail)
		fs.createDirectory(new VariableAutomaton("/a/"));
	}

	@Test
	public void testIsDirectoryAndIsRegularFile() throws FileStructureException {
		mkdir("/dir1");
		assertTrue(fs.isDirectory(new VariableAutomaton("/dir1")));
		assertTrue(fs.isDirectory(new VariableAutomaton("/dir1/")));
		assertFalse(fs.isRegularFile(new VariableAutomaton("/dir1")));
		assertFalse(fs.isRegularFile(new VariableAutomaton("/dir1/")));
		assertFalse(fs.isDirectory(new VariableAutomaton("/dir1/blah")));
		assertFalse(fs.isDirectory(new VariableAutomaton("/dir1blah")));

		fs = new FileStructure();
		touch("/file1");
		assertFalse(fs.isDirectory(new VariableAutomaton("/file1")));
		assertFalse(fs.isDirectory(new VariableAutomaton("/file1/")));
		assertTrue(fs.isRegularFile(new VariableAutomaton("/file1")));
		assertTrue(fs.isRegularFile(new VariableAutomaton("/file1/")));
		assertFalse(fs.isRegularFile(new VariableAutomaton("/file1blah")));
	}

	void touch(String fp) throws FileStructureException {
		fs.createFile(regex(fp));
	}

	void mkdir(String fp) throws FileStructureException {
		fs.createDirectory(regex(fp));
	}

	void copy(String src, String dest) throws FileStructureException {
		fs.copy(new VariableAutomaton(src), new VariableAutomaton(dest));
	}

	boolean exists(String fp) {
		return fs.fileExists(new VariableAutomaton(fp));
	}

	// returns a variable automaton given a regex
	VariableAutomaton regex(String regex) {
		return new VariableAutomaton(new RegExp(regex).toAutomaton());
	}

	void save(Automaton a, String filepath) {
		GraphvizGenerator.saveDOTToFile(a.toDot(), filepath);
	}

}
