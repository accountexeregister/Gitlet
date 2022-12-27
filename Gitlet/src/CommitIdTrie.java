import Utilities.Utils;
import org.junit.Test;

import java.io.Serializable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CommitIdTrie implements Serializable {
    // How deep the CommitIdTrie is, starting from 0 as the top level, and the one below as 1, and so on, increasing by 1 every time the trie goes deeper
    private int depth;
    // Name of the full commit id
    private String commitId;
    // Size of this array 16, based on the sha1 hexadecimal digits, which are 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, a, b, c, d, e and f
    private CommitIdTrie[] commitIdTriesArr;

    public CommitIdTrie() {
        this.depth = 0;
        commitIdTriesArr = new CommitIdTrie[16];
    }

    private CommitIdTrie(int parentDepth) {
        this.depth = parentDepth + 1;
        commitIdTriesArr = new CommitIdTrie[16];
    }

    public void addCommitId(String commitId) {
        String commitIdChar = commitId.charAt(depth) + "";
        int commitIdCharHexadecimal = Integer.parseInt(commitIdChar, 16);
        if (commitIdTriesArr[commitIdCharHexadecimal] == null) {
            commitIdTriesArr[commitIdCharHexadecimal] = new CommitIdTrie(depth);
            commitIdTriesArr[commitIdCharHexadecimal].commitId = commitId;
        } else {
            if (commitIdTriesArr[commitIdCharHexadecimal].commitId != null) {
                commitIdTriesArr[commitIdCharHexadecimal].addCommitId(commitIdTriesArr[commitIdCharHexadecimal].commitId);
                commitIdTriesArr[commitIdCharHexadecimal].commitId = null;
            }
            commitIdTriesArr[commitIdCharHexadecimal].addCommitId(commitId);

        }
        saveTrie();
    }

    public String searchCommitId(String commitId, CommitIdTrie currentTrie) {
        String commitIdChar = commitId.charAt(depth) + "";
        int commitIdCharHexadecimal = Integer.parseInt(commitIdChar, 16);

        if (commitIdTriesArr[commitIdCharHexadecimal] == null) {
            if (depth > 0) {
                return currentTrie.commitId;
            } else {
                return null;
            }
        }

        return searchCommitId(commitId, commitIdTriesArr[commitIdCharHexadecimal]);
    }

    public void saveTrie() {
        if (Repository.COMMIT_ID_TRIE.exists()) {
            Utils.writeObject(Repository.COMMIT_ID_TRIE, this);
        }
    }

    @Test
    public void testAddCommitId01() {
        String sampleCommitId = "a0da1ea5a15ab613bf9961fd86f010cf74c7ee48";
        CommitIdTrie commitIdTrie = new CommitIdTrie();
        commitIdTrie.addCommitId(sampleCommitId);
        assertEquals(sampleCommitId, commitIdTrie.commitIdTriesArr[10].commitId);
    }

    @Test
    public void testAddCommitId02() {
        String sampleCommitId1 = "a0da1ea5a15ab613bf9961fd86f010cf74c7ee48";
        String sampleCommitId2 = "a7da1ea5a15ab613bf9961fd86f010cf74c7ee48";
        CommitIdTrie commitIdTrie = new CommitIdTrie();
        commitIdTrie.addCommitId(sampleCommitId1);
        commitIdTrie.addCommitId(sampleCommitId2);
        assertNull(commitIdTrie.commitIdTriesArr[10].commitId);
        assertEquals(sampleCommitId1, commitIdTrie.commitIdTriesArr[10].commitIdTriesArr[0].commitId);
        assertEquals(sampleCommitId2, commitIdTrie.commitIdTriesArr[10].commitIdTriesArr[7].commitId);
    }

    @Test
    public void testAddCommitId03() {
        String sampleCommitId1 = "a0da1ea5a15ab613bf9961fd86f010cf74c7ee48";
        String sampleCommitId2 = "a7da1ea5a15ab613bf9961fd86f010cf74c7ee48";
        String sampleCommitId3 = "a7ca1ea5a15ab613bf9961fd86f010cf74c7ee48";
        CommitIdTrie commitIdTrie = new CommitIdTrie();
        commitIdTrie.addCommitId(sampleCommitId1);
        commitIdTrie.addCommitId(sampleCommitId2);
        commitIdTrie.addCommitId(sampleCommitId3);
        assertNull(commitIdTrie.commitIdTriesArr[10].commitId);
        assertEquals(sampleCommitId1, commitIdTrie.commitIdTriesArr[10].commitIdTriesArr[0].commitId);
        assertNull(commitIdTrie.commitIdTriesArr[10].commitIdTriesArr[7].commitId);
        assertEquals(sampleCommitId2, commitIdTrie.commitIdTriesArr[10].commitIdTriesArr[7].commitIdTriesArr[13].commitId);
        assertEquals(sampleCommitId3, commitIdTrie.commitIdTriesArr[10].commitIdTriesArr[7].commitIdTriesArr[12].commitId);
    }

    @Test
    public void testAddCommitId04() {
        String sampleCommitId1 = "a0da1ea5a15ab613bf9961fd86f010cf74c7ee48";
        String sampleCommitId2 = "a0da24a5a15ab613bf9961fd86f010cf74c7ee48";
        CommitIdTrie commitIdTrie = new CommitIdTrie();
        commitIdTrie.addCommitId(sampleCommitId1);
        commitIdTrie.addCommitId(sampleCommitId2);
        assertNull(commitIdTrie.commitIdTriesArr[10].commitId);
        assertNull(commitIdTrie.commitIdTriesArr[10].commitIdTriesArr[0].commitId);
        assertNull(commitIdTrie.commitIdTriesArr[10].commitIdTriesArr[0].commitIdTriesArr[13].commitId);
        assertNull(commitIdTrie.commitIdTriesArr[10].commitIdTriesArr[0].commitIdTriesArr[13].commitIdTriesArr[10].commitId);
        assertEquals(sampleCommitId1, commitIdTrie.commitIdTriesArr[10].commitIdTriesArr[0].commitIdTriesArr[13].commitIdTriesArr[10].commitIdTriesArr[1].commitId);
        assertEquals(sampleCommitId2, commitIdTrie.commitIdTriesArr[10].commitIdTriesArr[0].commitIdTriesArr[13].commitIdTriesArr[10].commitIdTriesArr[2].commitId);
    }

    @Test
    public void testAddCommitId05() {
        String sampleCommitId1 = "a0da1ea5a15ab613bf9961fd86f010cf74c7ee48";
        String sampleCommitId2 = "a0da21a5a15ab613bf9961fd86f010cf74c7ee48";
        String sampleCommitId3 = "a0da23a5a15ab613bf9961fd86f010cf74c7ee48";
        String sampleCommitId4 = "a0da2345a15ab613bf9961fd86f010cf74c7ee48";
        String sampleCommitId5 = "a0da2355a15ab613bf9961fd86f010cf74c7ee48";

        CommitIdTrie commitIdTrie = new CommitIdTrie();
        commitIdTrie.addCommitId(sampleCommitId1);
        commitIdTrie.addCommitId(sampleCommitId2);
        commitIdTrie.addCommitId(sampleCommitId3);
        commitIdTrie.addCommitId(sampleCommitId4);
        commitIdTrie.addCommitId(sampleCommitId5);
        assertNull(commitIdTrie.commitIdTriesArr[10].commitId);
        assertNull(commitIdTrie.commitIdTriesArr[10].commitIdTriesArr[0].commitId);
        assertNull(commitIdTrie.commitIdTriesArr[10].commitIdTriesArr[0].commitIdTriesArr[13].commitId);
        assertNull(commitIdTrie.commitIdTriesArr[10].commitIdTriesArr[0].commitIdTriesArr[13].commitIdTriesArr[10].commitId);
        assertNull(commitIdTrie.commitIdTriesArr[10].commitIdTriesArr[0].commitIdTriesArr[13].commitIdTriesArr[10].commitIdTriesArr[2].commitId);
        assertNull(commitIdTrie.commitIdTriesArr[10].commitIdTriesArr[0].commitIdTriesArr[13].commitIdTriesArr[10].commitIdTriesArr[2].commitIdTriesArr[3].commitId);
        assertEquals(sampleCommitId1, commitIdTrie.commitIdTriesArr[10].commitIdTriesArr[0].commitIdTriesArr[13].commitIdTriesArr[10].commitIdTriesArr[1].commitId);
        assertEquals(sampleCommitId2, commitIdTrie.commitIdTriesArr[10].commitIdTriesArr[0].commitIdTriesArr[13].commitIdTriesArr[10].commitIdTriesArr[2].commitIdTriesArr[1].commitId);
        assertEquals(sampleCommitId3, commitIdTrie.commitIdTriesArr[10].commitIdTriesArr[0].commitIdTriesArr[13].commitIdTriesArr[10].commitIdTriesArr[2].commitIdTriesArr[3].commitIdTriesArr[10].commitId);
        assertEquals(sampleCommitId4, commitIdTrie.commitIdTriesArr[10].commitIdTriesArr[0].commitIdTriesArr[13].commitIdTriesArr[10].commitIdTriesArr[2].commitIdTriesArr[3].commitIdTriesArr[4].commitId);
        assertEquals(sampleCommitId5, commitIdTrie.commitIdTriesArr[10].commitIdTriesArr[0].commitIdTriesArr[13].commitIdTriesArr[10].commitIdTriesArr[2].commitIdTriesArr[3].commitIdTriesArr[5].commitId);
    }

}
