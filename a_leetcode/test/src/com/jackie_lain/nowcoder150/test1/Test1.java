package com.jackie_lain.nowcoder150.test1;



/**
 * @version : 1.0
 * @description
 * @auther : 赖钒
 * @date : 2025-11-04 下午6:28
 */


public class Test1 {

    public static void main(String[] args) {
        Solution solution = new Solution();
        ListNode head = new ListNode(1);
        head.next = new ListNode(2);
        head.next.next = new ListNode(3);

        ListNode resultListNode = solution.ReverseList(head);
        while(resultListNode!=null){
            System.out.println(resultListNode.val);
            resultListNode = resultListNode.next;
        }
    }
}

class ListNode {
    int val;
    ListNode next = null;

    ListNode(int val) {
        this.val = val;
    }
}
class Solution {
    /**
     * 代码中的类名、方法名、参数名已经指定，请勿修改，直接返回方法规定的值即可
     *
     *
     * @param head ListNode类
     * @return ListNode类
     */
    public ListNode ReverseList (ListNode head) {
        // write code here
        ListNode resultListNode;

        ListNode sign = head;

        ListNode nextNode1 = null;

        if(head!=null) {
            while (sign.next != null) {
                sign = sign.next;
            }

            resultListNode = sign;

            while (head != sign) {
                nextNode1 = head.next;
                head.next = resultListNode.next;
                resultListNode.next = head;
                head = nextNode1;
            }
        }else{
            resultListNode = null;
        }
        return resultListNode;
    }
}