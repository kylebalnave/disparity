/*
 * Copyright (C) 2014 balnave
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package disparity;

import disparity.results.ImageDifferenceResult;
import io.FileUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import semblance.results.IResult;
import semblance.runners.Runner;

/**
 * Compares two screen-shots and creates a difference image The primary use of
 * this class is for CSS regression testing
 *
 * @author balnave
 */
public class Disparity extends Runner {

    public Disparity(Map config) {
        super(config);
    }

    /**
     * Gets the folders with timestamp names
     *
     * @param rootDir
     * @return
     */
    private List<File> getTimestampedDirs(File rootDir) {
        List<File> allDirs = FileUtils.listFolders(rootDir);
        //
        // only allow folders that have the timestamp
        for (int i = allDirs.size() - 1; i >= 0; i--) {
            // match folders with correct naming
            // yyyy-MM-dd HH:mm:ss
            if (!allDirs.get(i).getName().matches("^\\d{4}-\\d{2}-\\d{2}\\s{1}\\d{2}-\\d{2}-\\d{2}$")) {
                allDirs.remove(i);
            }
        }
        Collections.sort(allDirs);
        return allDirs;
    }

    /**
     * Gets matching files in the newest and oldest folders
     *
     * @param rootDir
     * @return
     */
    private List<File[]> getMatchingFilesByName(File rootDir) {
        List<File[]> matchingFiles = new ArrayList<File[]>();
        List<File> matchedDirs = getTimestampedDirs(rootDir);
        if (!matchedDirs.isEmpty()) {
            Map<String, File> refFileList01 = FileUtils.listFiles(matchedDirs.get(0).getAbsolutePath());
            Map<String, File> refFileList02 = FileUtils.listFiles(matchedDirs.get(matchedDirs.size() - 1).getAbsolutePath());
            for (String key : refFileList01.keySet()) {
                if (refFileList02.containsKey(key)) {
                    File[] match = new File[]{refFileList01.get(key), refFileList02.get(key)};
                    matchingFiles.add(match);
                }
            }
        }
        return matchingFiles;
    }

    @Override
    /**
     * Compares images and creates a diff image
     */
    public List<IResult> run() throws Exception {
        ExecutorService execSvc = Executors.newFixedThreadPool(((Number) getConfigValue("threads", 10)).intValue());
        File dirIn = new File((String) getConfigValue("in"));
        File dirOut = new File((String) getConfigValue("out"));
        Number fuzzyness = (Number) getConfigValue("fuzzyness");
        List<File[]> matchedFiles = getMatchingFilesByName(dirIn);
        if (!matchedFiles.isEmpty()) {
            File diffOutDir = new File(dirOut, "diff");
            diffOutDir.mkdirs();
            List<SingleImageDiff> queue = new ArrayList<SingleImageDiff>();
            for (File[] matched : matchedFiles) {
                queue.add(new SingleImageDiff(matched[0], matched[1], diffOutDir, fuzzyness.intValue()));
            }
            List<Future<List<IResult>>> futureResults = execSvc.invokeAll(queue);
            for (Future<List<IResult>> res : futureResults) {
                if (res.get() != null) {
                    results.addAll(res.get());
                }
            }
        } else {
            results.add(new ImageDifferenceResult(
                    dirIn.getAbsolutePath(),
                    false,
                    String.format("Image Difference Error: %s", "No images to compare!")
            ));
        }
        return results;
    }

}
