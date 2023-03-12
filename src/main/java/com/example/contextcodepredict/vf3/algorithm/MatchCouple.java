package com.example.contextcodepredict.vf3.algorithm;

import com.example.contextcodepredict.vf3.graph.Vertex;

/**
 * MatchCouple 匹配节点对类
 */
public class MatchCouple {
  /**
   * 模式图节点
   */
  private Vertex u;
  /**
   * 目标图节点
   */
  private Vertex v;

  public MatchCouple() {
  }

  public MatchCouple(Vertex u, Vertex v) {
    this.u = u;
    this.v = v;
  }

  public Vertex getU() {
    return u;
  }

  public void setU(Vertex u) {
    this.u = u;
  }

  public Vertex getV() {
    return v;
  }

  public void setV(Vertex v) {
    this.v = v;
  }

  @Override
  public String toString() {
    return "MatchCouple{" + "u=" + u + ", v=" + v + '}';
  }
}
