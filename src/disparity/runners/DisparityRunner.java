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
package disparity.runners;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import semblance.io.FileUtils;
import semblance.runners.MultiThreadRunner;
import semblance.runners.Runner;

/**
 * Compares two screen-shots and creates a difference image The primary use of
 * this class is for CSS regression testing
 *
 * @author balnave
 */
public class DisparityRunner extends MultiThreadRunner {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        callRunnerSequence(DisparityRunner.class, args);
    }

    public DisparityRunner(Map config) {
        super(config);
    }

    public DisparityRunner(String configUrlOrFilePath) {
        super(configUrlOrFilePath);
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

    /**
     * Generate the individual Runner threads
     * @return 
     */
    @Override
    protected List<Runner> getRunnerCollection() {
        File dirIn = new File((String) getConfigValue("in", "./"));
        File dirOut = new File((String) getConfigValue("out", "./"));
        Number fuzzyness = (Number) getConfigValue("fuzzyness", 0);
        List<File[]> matchedFiles = getMatchingFilesByName(dirIn);
        List<Runner> queue = new ArrayList<Runner>();
        if (!matchedFiles.isEmpty()) {
            File diffOutDir = new File(dirOut, "diff");
            diffOutDir.mkdirs();
            for (File[] matched : matchedFiles) {
                queue.add(new SingleImageDiff(matched[0], matched[1], diffOutDir, fuzzyness.intValue()));
            }
        }
        return queue;
    }

}
