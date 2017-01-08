/**
 * This class represents a file structure using an automaton.
 * 
 * @author Rodney Rodriguez
 *
 */
package edu.utsa.fileflow.client.fileflow;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.FiniteStateTransducer;

public class FileStructure implements Cloneable {

	static {
		Automaton.setMinimization(Automaton.MINIMIZE_BRZOZOWSKI);
		Automaton.setMinimizeAlways(true);
	}

	private static final Automaton SEPARATOR = Automaton.makeChar('/');
	private static final FiniteStateTransducer FST_PARENT = FiniteStateTransducer.parentDir();

	private VariableAutomaton cwd = new VariableAutomaton("/");
	Automaton files;

	public FileStructure() {
		this.files = SEPARATOR.clone();
	}

	public static FileStructure top() {
		Automaton files = SEPARATOR.clone();
		files.concatenate(Automaton.makeAnyString());
		return new FileStructure(files);
	}

	private FileStructure(Automaton files) {
		this.files = files;
	}

	/**
	 * @return a clone of the separator automaton.
	 */
	public static Automaton separator() {
		return SEPARATOR.clone();
	}

	/**
	 * Prepends the current working directory to the file path variable given.
	 * 
	 * @param fp
	 *            The file path to be appended to the current working directory.
	 * @return the absolute file path as an automaton.
	 */
	private VariableAutomaton makeAbsolute(VariableAutomaton fp) {
		return cwd.concat(fp);
	}

	/**
	 * Creates a file at the file path provided. Parent directory must exist for
	 * this operation to be successful. <code>fp</code> must not represent a
	 * directory (include a trailing slash).
	 * 
	 * @param fp
	 *            The file path to create the file.
	 * @throws FileStructureException
	 *             if the parent directory does not exist
	 */
	public void createFile(VariableAutomaton fp) throws FileStructureException {
		Automaton a = makeAbsolute(fp).getAutomaton();
		if (fp.isDirectory())
			throw new FileStructureException(
					"touch: cannot touch '" + a.getCommonPrefix() + "': Cannot touch a directory");
		Automaton parent = getParentDirectory(a);
		if (!directoryExists(parent))
			throw new FileStructureException(
					"touch: cannot touch " + a.getCommonPrefix() + ": No such file or directory");
		files = files.union(a);
	}

	public void createDirectory(VariableAutomaton fp) {
		VariableAutomaton va = fp;
		if (!fp.endsWith(SEPARATOR))
			va = fp.concat(new VariableAutomaton("/"));
		Automaton a = makeAbsolute(va).getAutomaton();
		files = files.union(a);
	}

	public boolean directoryExists(String fp) {
		fp = cleanDir(fp);
		return files.run(fp);
	}

	public boolean directoryExists(Automaton fp) {
		return fp.subsetOf(files);
	}

	public static Automaton getParentDirectory(Automaton a) {
		return FST_PARENT.intersection(a);
	}

	/**
	 * This is a special version of {@link Automaton#makeString}. It creates an
	 * automaton representation of a file that is compatible with and can be
	 * inserted into a {@link FileStructure}.
	 * 
	 * @param fp
	 *            The String representation of the file path.
	 * @return an automaton representation of <code>fp</code>.
	 */
	public static Automaton makeFileAutomaton(String fp) {
		return new VariableAutomaton("/" + fp).getAutomaton();
	}

	public static Automaton makeDirAutomaton(String fp) {
		fp = cleanDir(fp);
		return makeFileAutomaton(fp);
	}

	/**
	 * Makes an {@link Automaton} representing a file path. All separators will
	 * be accept states in the returned automaton.
	 * 
	 * @param filepath
	 *            A string representing the file path.
	 * @return <code>filepath</code> as an Automaton.
	 */
	public static Automaton makeFilepath(String filepath) {
		Automaton a;
		boolean startsWithSlash = (filepath.startsWith("/") || filepath.startsWith("\\"));
		boolean endsWithSlash = (filepath.endsWith("/") || filepath.endsWith("\\"));
		StringBuilder sb = new StringBuilder();

		filepath = FileStructure.clean(filepath);
		String[] l = filepath.split("/");

		// add a single initial slash if fp starts with slash
		if (startsWithSlash) {
			a = Automaton.makeChar('/');
		} else {
			a = Automaton.makeEmpty();
		}

		// build the automaton
		for (int i = 0; i < l.length; i++) {
			sb.append(l[i]);
			if (i != l.length - 1)
				sb.append('/');
			else if (endsWithSlash)
				sb.append('/');
			a = a.union(Automaton.makeString(sb.toString()));
		}
		return a;
	}

	/**
	 * Cleans a string representing a file path.
	 * 
	 * @param fp
	 *            The file path to clean.
	 * @return a new String with the cleaned file path.
	 */
	public static String clean(String fp) {
		fp = fp.trim();
		fp = fp.replaceAll("[/\\\\]+", "/");
		return fp;
	}

	/**
	 * Cleans a file path and appends a SLASH if fp does not end with one.
	 * 
	 * @param fp
	 *            The file path to clean.
	 * @return a new String with the cleaned directory file path.
	 */
	public static String cleanDir(String fp) {
		clean(fp);
		if (!fp.endsWith("/"))
			fp = fp.concat("/");
		return fp;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FileStructure))
			return false;
		FileStructure o = (FileStructure) obj;
		if (!files.equals(o.files))
			return false;
		return true;
	}

	/**
	 * Graphviz DOT representation of this file structure.
	 * 
	 * @return a Graphviz DOT representation of the files automaton.
	 */
	public String toDot() {
		return files.toDot();
	}

	@Override
	protected FileStructure clone() {
		FileStructure clone = new FileStructure();
		clone.files = files.clone();
		return clone;
	}

}
