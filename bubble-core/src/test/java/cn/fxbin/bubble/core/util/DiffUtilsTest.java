package cn.fxbin.bubble.core.util;

import com.github.difflib.patch.Patch;
import com.github.difflib.text.DiffRow;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;


/**
 * DiffUtilsTest
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/12/11 10:07
 */
public class DiffUtilsTest {


    @Test
    void testDiffAndPatch() {
        List<String> original = Arrays.asList("line1", "line2", "line3");
        List<String> revised = Arrays.asList("line1", "line2 changed", "line3");

        Patch<String> patch = DiffUtils.diff(original, revised);
        Assertions.assertNotNull(patch);
        Assertions.assertFalse(patch.getDeltas().isEmpty());

        List<String> patched = DiffUtils.patch(original, patch);
        Assertions.assertEquals(revised, patched);
    }

    @Test
    void testUnifiedDiff() {
        List<String> original = Arrays.asList("line1", "line2", "line3");
        List<String> revised = Arrays.asList("line1", "line2 changed", "line3");

        List<String> diff = DiffUtils.unifiedDiff(original, revised, 1);
        Assertions.assertNotNull(diff);
        Assertions.assertTrue(diff.size() > 0);

        // Basic check for header
        Assertions.assertTrue(diff.get(0).startsWith("---"));
        Assertions.assertTrue(diff.get(1).startsWith("+++"));
    }

    @Test
    void testDiffRows() {
        List<String> original = Arrays.asList("This is a test sentence.");
        List<String> revised = Arrays.asList("This is a test for diffutils.");

        List<DiffRow> rows = DiffUtils.diffRows(original, revised, true);
        Assertions.assertNotNull(rows);
        Assertions.assertFalse(rows.isEmpty());

        DiffRow row = rows.get(0);
        Assertions.assertNotNull(row.getOldLine());
        Assertions.assertNotNull(row.getNewLine());

        // Check that some diff tags are present (default tags might be html or similar depending on generator defaults,
        // but we just checked non-null result and execution)
    }

}
