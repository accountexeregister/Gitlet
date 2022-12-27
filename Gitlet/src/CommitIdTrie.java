public class CommitIdTrie {
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

    public CommitIdTrie(int parentDepth) {
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
            commitIdTriesArr[commitIdCharHexadecimal].addCommitId(this.commitId);
            commitIdTriesArr[commitIdCharHexadecimal].addCommitId(commitId);
            this.commitId = null;
        }
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
}
