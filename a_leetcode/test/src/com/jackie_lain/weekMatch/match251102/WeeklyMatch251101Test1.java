package com.jackie_lain.weekMatch.match251102;

import java.util.ArrayList;
import java.util.List;

/**
 * @version : 1.0
 * @description
 * @auther : jackie_lain
 * @date : 2025-11-03 下午9:43
 */
public class WeeklyMatch251101Test1 {

    public static void main(String[] args) {
        Solution solution = new Solution();
        int[] nums = {4, 2, 7, 5, 9, 1, 3};
        List<Integer> missingElements = solution.findMissingElements(nums);
        System.out.println(missingElements); // 输出: [6, 8]
    }
}

class Solution {
    public List<Integer> findMissingElements(int[] nums) {
        List<Integer> res = new ArrayList<>();

        boolean[] sign = new boolean[101];
        int min = 101;
        int max = -1;

        for (int i = 0; i < nums.length; i++) {
            sign[nums[i]] = true;
            min = Math.min(min, nums[i]);
            max = Math.max(max, nums[i]);
        }

        for (int i = min; i <= max; i++) {
            if (!sign[i]) {
                res.add(i);
            }
        }
        return res;
    }
}