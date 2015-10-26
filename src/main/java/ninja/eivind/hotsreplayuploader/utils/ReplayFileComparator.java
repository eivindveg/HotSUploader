package ninja.eivind.hotsreplayuploader.utils;

import ninja.eivind.hotsreplayuploader.models.ReplayFile;
import ninja.eivind.hotsreplayuploader.models.Status;

import java.io.File;
import java.util.Comparator;

/**
 * @author Eivind Vegsundvåg
 */
public class ReplayFileComparator implements Comparator<ReplayFile> {
    @Override
    public int compare(ReplayFile o1, ReplayFile o2) {
        if(o1.getStatus() == Status.EXCEPTION && o2.getStatus() != Status.EXCEPTION) {
            return 1;
        } else if(o2.getStatus() == Status.EXCEPTION && o1.getStatus() != Status.EXCEPTION) {
            return -1;
        }

        File file1 = o1.getFile();
        File file2 = o2.getFile();

        return -Long.compare(file1.lastModified(), file2.lastModified());
    }
}
