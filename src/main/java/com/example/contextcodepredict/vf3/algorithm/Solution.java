package com.example.contextcodepredict.vf3.algorithm;

import java.util.ArrayList;

/**
 * solution 映射结果类
 */
public class Solution {
  /**
   * 解决方案，匹配节点对的集合
   */
  private ArrayList<MatchCouple> solution;

  public Solution() {
  }

  public Solution(ArrayList<MatchCouple> solution) {
    this.solution = solution;
  }

  public ArrayList<MatchCouple> getSolution() {
    return solution;
  }

  public void setSolution(ArrayList<MatchCouple> solution) {
    this.solution = solution;
  }

  @Override
  public String toString() {
    return "Solution{" + "solution=" + solution + '}';
  }
}
