package edu.utsa.fileflow.client.fileflow;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import edu.utsa.fileflow.testutils.GraphvizGenerator;

public class CopyTest {

	FileStructure fs;

	@Before
	public void setUp() throws Exception {
		Automaton.setMinimization(Automaton.MINIMIZE_BRZOZOWSKI);
		Automaton.setMinimizeAlways(true);
		fs = new FileStructure();
	}

	@Test(expected = FileStructureException.class)
	public void testCopySourceDoesNotExist() throws FileStructureException {
		copy("/fake", "/");
	}

	@Test(expected = FileStructureException.class)
	public void testCopyDestDoesNotExist() throws FileStructureException {
		touch("/a");
		copy("/a", "/fakedir/fakefile");
	}

	@Test(expected = FileStructureException.class)
	public void testCopySamePath() throws FileStructureException {
		touch("/a");
		copy("/a", "/a");
	}

	@Test
	public void testCopyFileToFile() throws FileStructureException {
		// test when dest file does NOT exist
		// 'home/user/a' should be created
		mkdir("/tmp/");
		mkdir("/home/user/");
		touch("/tmp/a");
		touch("/tmp/b");
		copy("/tmp/a", "/home/user/a");
		assertTrue(exists("/tmp/a"));
		assertTrue(fs.isRegularFile(new VariableAutomaton("/home/user/a")));
		assertFalse(fs.isDirectory(new VariableAutomaton("/home/user/a")));
		assertFalse(exists("/home/user/b"));

		// test when dest file DOES exist
		// 'tmp/a' should overwrite 'home/user/a'
		fs = new FileStructure();
		mkdir("/tmp/");
		mkdir("/home/user/");
		touch("/tmp/a");
		touch("/tmp/b");
		touch("/home/user/a");
		assertTrue(exists("/home/user/a"));
		assertTrue(fs.isRegularFile(new VariableAutomaton("/home/user/a")));
		copy("/tmp/a", "/home/user/a");
		assertTrue(exists("/tmp/a"));
		assertTrue(fs.isRegularFile(new VariableAutomaton("/home/user/a")));
		assertFalse(fs.isDirectory(new VariableAutomaton("/home/user/a")));
		assertFalse(exists("/home/user/b"));

		// test when both files are in root directory
		fs = new FileStructure();
		touch("/a");
		copy("/a", "/b");
		assertTrue(exists("/a"));
		assertTrue(exists("/b"));

		// test when both files are in root directory without slashes
		fs = new FileStructure();
		touch("a");
		copy("a", "b");
		assertTrue(exists("/a"));
		assertTrue(exists("/b"));
	}

	@Test
	public void testCopyFileToDir() throws FileStructureException {
		touch("/a");
		mkdir("/b/");
		copy("/a", "/b");
		assertTrue(exists("/a"));
		assertTrue(exists("/b/"));
		assertTrue(exists("/b/a"));
		assertTrue(fs.isDirectory(regex("/b")));
		assertFalse(fs.isRegularFile(regex("/b")));
		assertTrue(fs.isRegularFile(regex("/b/a")));
		assertFalse(fs.isDirectory(regex("/b/a")));
	}

	@Test
	public void testCopyDirToDir() throws FileStructureException {
		// cp /home/user/ /dir1/dir2/
		mkdir("/dir1/dir2/");
		mkdir("/root/");
		mkdir("/home/user/");
		touch("/home/user/bashrc");
		save(fs.files, "/test/fs/copy_files.orig.dot");
		assertTrue(exists("/dir1/dir2"));
		assertTrue(exists("/root/"));
		assertTrue(exists("/home/user/bashrc"));

		copy("/home/user/", "/dir1/dir2");
		save(fs.files, "/test/fs/copy_files.dot");
		assertTrue(exists("/dir1/dir2/user/"));
		assertTrue(fs.isDirectory(new VariableAutomaton("/dir1/dir2/user/")));
		assertFalse(fs.isRegularFile(new VariableAutomaton("/dir1/dir2/user/")));
		assertTrue(exists("/dir1/dir2/user/bashrc"));
		assertTrue(fs.isRegularFile(new VariableAutomaton("/dir1/dir2/user/bashrc")));
		assertFalse(fs.isDirectory(new VariableAutomaton("/dir1/dir2/user/bashrc")));
	}

	@Test(expected = FileStructureException.class)
	public void testCopyDirToFile() throws FileStructureException {
		mkdir("a");
		touch("b");
		copy("a", "b");
	}

	@Test(expected = FileStructureException.class)
	public void testCopyFileToNonExisting() throws FileStructureException {
		touch("a");
		copy("a", "fake/");
	}

	@Test(expected = FileStructureException.class)
	public void testCopyDirToNonExisting() throws FileStructureException {
		mkdir("/a");
		copy("a", "fake/fake");
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
