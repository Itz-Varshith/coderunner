#include <bits/stdc++.h>
using namespace std;

int main() {
    // Your code here
    int n;
    cin>>n;
    vector<int> v(n);
    for(int i=0;i<n;i++){
        cin>>v[i];
    }
    sort(v.begin(), v.end());

    for(auto& it : v){
        cout<<it<<" ";
    }
    cout<<endl;
    return 0;
}
