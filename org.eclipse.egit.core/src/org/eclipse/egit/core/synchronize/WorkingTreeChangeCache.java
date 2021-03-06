/*******************************************************************************
 * Copyright (C) 2011, Dariusz Luksza <dariusz@luksza.org>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.egit.core.synchronize;

import static org.eclipse.egit.core.synchronize.GitCommitsModelCache.RIGHT;
import static org.eclipse.egit.core.synchronize.GitCommitsModelCache.calculateAndSetChangeKind;
import static org.eclipse.jgit.treewalk.filter.TreeFilter.ANY_DIFF;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.egit.core.Activator;
import org.eclipse.egit.core.synchronize.GitCommitsModelCache.Change;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.MutableObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.NotIgnoredFilter;

/**
 * Builds list of working tree changes.
 */
public class WorkingTreeChangeCache {

	/**
	 * @param repo
	 *            with should be scanned
	 * @return list of changes in working tree
	 */
	public static Map<String, Change> build(Repository repo) {
		TreeWalk tw = new TreeWalk(repo);
		try {
			tw.addTree(new FileTreeIterator(repo));
			tw.addTree(new DirCacheIterator(repo.readDirCache()));
			tw.setFilter(AndTreeFilter.create(new NotIgnoredFilter(0), ANY_DIFF));
			tw.setRecursive(true);

			Map<String, Change> result = new HashMap<String, Change>();
			MutableObjectId idBuf = new MutableObjectId();
			while (tw.next()) {
				Change change = new Change();
				change.name = tw.getNameString();
				tw.getObjectId(idBuf, 0);
				change.objectId = AbbreviatedObjectId.fromObjectId(idBuf);
				tw.getObjectId(idBuf, 1);
				change.remoteObjectId = AbbreviatedObjectId.fromObjectId(idBuf);
				calculateAndSetChangeKind(RIGHT, change);

				result.put(tw.getPathString(), change);
			}
			tw.release();

			return result;
		} catch (IOException e) {
			Activator.error(e.getMessage(), e);
			return new HashMap<String, GitCommitsModelCache.Change>(0);
		}
	}

}
