# Outdated Fact Detection in KBs

This is the code for the paper: [Outdated Fact Detection in Knowledge Bases](https://ieeexplore.ieee.org/document/9101535),
Shuang Hao, Chengliang Chai, Guoliang Li, Nan Tang, Ning Wang, Xiang Yu,
which was presented at [ICDE 2020](https://icde.utdallas.edu/).

## Framework

![](https://github.com/Shuang-H/outdated-fact-detection/blob/main/framework.jpg)

- Phase I:  Outdated Fact Prediction. The input is the KB and reference data on hand, and the output is the OD Model that predicts the likelihood of each fact being outdated, which provides evidence for Phase II.
- Phase II: Human-based Verification. It takes the KB, logical rules, and prediction results of OD Model as the input to build a graph model for selecting facts to be verified, and outputs the human answers of the selected facts.
- Phase III: Rule-based Fact Expansion. It takes the human labels from  Phase II as input, infers more outdated facts, and feeds these inferred facts into the ML model (Phase I).

Here, we provide the code of `'Top-k Hits Selection & Fact Expansion'` and `'Logical Rule Mining'`

## Citations

If you find this code useful in your research then please cite
````
@inproceedings{hao2020outdated,
  title={Outdated Fact Detection in Knowledge Bases},
  author={Hao, Shuang and Chai, Chengliang and Li, Guoliang and Tang, Nan and Wang, Ning and Yu, Xiang},
  booktitle={2020 IEEE 36th International Conference on Data Engineering (ICDE)},
  pages={1890--1893},
  year={2020},
  organization={IEEE}
}
````
