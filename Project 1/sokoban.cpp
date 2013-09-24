#include <iostream>
#include <stdio.h>
#include <vector>
using namespace std;
int maxY = 8;
int maxX = 7;

int solution = 0; 
// This is for getting the correct plan from the plan vector
// Probably not best practices
vector<vector<int> >  goLeft(vector <vector <int> > current){

    int robotX = 0;
    int robotY = 0;
     for (int i=0;i<maxY;i++){
        for(int j =0;j<maxX;j++){
            if (current[i][j] == 2){
				robotX = j;
				robotY = i;
			}
		}
	}

	if ((current[robotY][robotX-1] >2) && (current[robotY][robotX-2] == 1)){
        current[robotY][robotX-2] = current[robotY][robotX-1];
        current[robotY][robotX-1] = 2;
        current[robotY][robotX] = 1;
	}
    else if (current[robotY][robotX-1] == 1){
        current[robotY][robotX-1] =2;
        current[robotY][robotX] = 1;
    }
    return current;
}
vector<vector<int> >  goRight(vector <vector <int> > current){
    int robotX = 0;
    int robotY = 0;
 	for (int i=0;i<maxY;i++){
		for(int j =0;j<maxX;j++){
			if (current[i][j] == 2){
				robotX = j;
				robotY = i;
			}
		}
	}
	if ((current[robotY][robotX+1] >2) && (current[robotY][robotX+2] == 1)){
        current[robotY][robotX+2] = current[robotY][robotX+1];
        current[robotY][robotX+1] = 2;
        current[robotY][robotX] = 1;
	}
    else if (current[robotY][robotX+1] == 1){
        current[robotY][robotX+1] =2;
        current[robotY][robotX] = 1;
    }
    
    return current;

}
vector<vector<int> >  goUp(vector <vector <int> > current){
    int robotX = 0;
    int robotY = 0;
 	for (int i=0;i<maxY;i++){
		for(int j =0;j<maxX;j++){
			if (current[i][j] == 2){
				robotX = j;
				robotY = i;
			}
		}
	}
	if ((current[robotY-1][robotX] >2) && (current[robotY-2][robotX] == 1)){
        current[robotY-2][robotX] = current[robotY-1][robotX];
        current[robotY-1][robotX] = 2;
        current[robotY][robotX] = 1;
	}
    else if (current[robotY-1][robotX] == 1){
        current[robotY-1][robotX] =2;
        current[robotY][robotX] = 1;
    }
    
    return current;
}
vector<vector<int> >  goDown(vector <vector <int> > current){
    int robotX = 0;
    int robotY = 0;
 	for (int i=0;i<maxY;i++){
		for(int j =0;j<maxX;j++){
			if (current[i][j] == 2){
				robotX = j;
				robotY = i;
			}
		}
	}
//cout << "Position: " << robotX << " " << robotY << endl;
	if ((current[robotY+1][robotX] >2) && (current[robotY+2][robotX] == 1)){
        current[robotY+2][robotX] = current[robotY+1][robotX];
        current[robotY+1][robotX] = 2;
        current[robotY][robotX] = 1;
	}
    else if (current[robotY+1][robotX] == 1){
        current[robotY+1][robotX] =2;
        current[robotY][robotX] = 1;
    }
    return current;
}
bool goalCheck(vector <vector <vector <int> > > frontier){
    bool goal = 0;
    for (int i=0;i<frontier.size();i++){
	//if (frontier[i][1][2] == 3){ //Problem 1
	//if ((frontier[i][1][1] ==3)&&(frontier[i][6][4] == 4)){ //Problem 2
	//if ((frontier[i][1][7] == 3)&&(frontier[i][2][7] == 4)&&(frontier[i][3][7] == 5)){ // Problem 3
        if ((frontier[i][4][2] >2)&&(frontier[i][4][3] >2)&&(frontier[i][4][4] >2)){ // Challenge
            goal = 1;
            for (int y=0;y<maxY;y++){
                for (int x=0;x<maxX;x++){
                    cout << frontier[i][y][x] << " ";
                }
	        cout << endl;
            }
	    solution = i;
        }
    }
    return goal;
}
int main(){
    vector<vector <int> > start(maxY,vector <int>(maxX,0));
/*  //Problem 2.1
    int world [7][6] ={
        {0,0,0,0,0,0},
        {0,1,1,0,0,0},
	{0,0,2,0,0,0},
	{0,1,1,1,1,0},
	{0,1,1,3,1,0},
	{0,1,1,0,0,0},
	{0,0,0,0,0,0}};
*/
/* //Problem 2.2
    int world [8][6] ={
        {0,0,0,0,0,0},
        {0,1,1,1,0,0},
	{0,2,3,4,1,0},
	{0,0,1,1,1,0},
	{0,0,0,1,1,0},
	{0,0,0,0,1,0},
	{0,0,0,0,1,0},
	{0,0,0,0,0,0}};
*/
/*  //Problem 2.3
    int world [8][11] ={
        {0,0,0,0,0,0,0,0,0,0,0},
	{0,0,0,0,0,0,0,1,1,1,0},
	{0,0,0,0,0,0,0,1,0,1,0},
        {0,0,0,0,0,0,0,1,0,1,0},
        {0,1,2,1,5,1,4,1,3,1,0},
        {0,1,0,1,0,1,0,1,0,0,0},
        {0,1,1,1,1,1,1,1,0,0,0},
	{0,0,0,0,0,0,0,0,0,0,0}};
*/

    int world [8][7] ={
        {0,0,0,0,0,0,0},
        {0,0,0,1,1,0,0},
	{0,0,0,3,1,0,0},
	{0,1,4,1,1,2,0},
	{0,1,1,1,1,1,0},
	{0,0,5,1,1,0,0},
	{0,0,1,1,0,0,0},
	{0,0,0,0,0,0,0}};

    for (int i=0;i<maxY;i++){
        for (int j=0;j<maxX;j++){
            start[i][j] =world[i][j];
        }
    }

    vector <vector <vector <int> > > frontier(0);
    vector <vector <vector <int> > > xplored(0);
    vector <vector <string> >frontierPlan(0);
    vector <vector <string> >xploredPlan(0);

    frontier.push_back(start);
    vector <string> tmp(0);
    tmp.push_back("Start");
    frontierPlan.push_back(tmp);
    int loops = 0;
    while ((!goalCheck(frontier))&&(loops < 100)){
        loops +=1;
        int size = frontier.size();


        for (int i=0;i<size;i++){
          frontier.push_back(goDown(frontier.at(0)));
	  tmp=frontierPlan.at(0);
	  tmp.push_back("Down");
	  frontierPlan.push_back(tmp);
		for (int j=0;j<frontier.size()-1;j++){		  
		  if (frontier.back() == frontier.at(j)){
			frontier.erase(frontier.end());
			frontierPlan.erase(frontierPlan.end());
		  }
		}

          frontier.push_back(goUp(frontier.at(0)));
	  tmp=frontierPlan.at(0);
	  tmp.push_back("Up");
	  frontierPlan.push_back(tmp);
		for (int j=0;j<frontier.size()-1;j++){
		  if (frontier.back() == frontier.at(j)){
			frontier.erase(frontier.end());
			frontierPlan.erase(frontierPlan.end());
		  }
		}

          frontier.push_back(goLeft(frontier.at(0)));
	  tmp=frontierPlan.at(0);
	  tmp.push_back("Left");
	  frontierPlan.push_back(tmp);
		for (int j=0;j<frontier.size()-1;j++){
		  if (frontier.back() == frontier.at(j)){
			frontier.erase(frontier.end());
			frontierPlan.erase(frontierPlan.end());
		  }
		}

          frontier.push_back(goRight(frontier.at(0)));
	  tmp=frontierPlan.at(0);
	  tmp.push_back("Right");
	  frontierPlan.push_back(tmp);
		for (int j=0;j<frontier.size()-1;j++){
		  if (frontier.back() == frontier.at(j)){
			frontier.erase(frontier.end());
			frontierPlan.erase(frontierPlan.end());
		  }
		}
	  for (int f=frontier.size()-1;f>-1;f--){
		int x = 0;
		bool flag = 1;
		while (flag && (x < xplored.size())){
		  if (xplored[x] == frontier[f]){
			frontier.erase(frontier.begin()+f);
		        frontierPlan.erase(frontierPlan.begin()+f);
			flag = 0;
		  }
		  x++;
		}
	  }

	  xplored.push_back(frontier.front());
	  for (int j=0;j<xplored.size()-1;j++){
		if (xplored.at(j) == xplored.back()){
			xplored.erase(xplored.end());
		}
	  }
          frontier.erase(frontier.begin());

	  frontierPlan.erase(frontierPlan.begin());
        }

       cout << frontier.size()<< "  " << xplored.size() << "  " <<loops << endl;
     }
    for (int i=0;i<frontierPlan[solution].size();i++){
	cout << frontierPlan[solution][i] << endl;
    }

    cout << endl;
cout << endl;
cout << frontierPlan[solution].size() << endl;
/*
for (int i=0;i<frontier.size();i++){
cout << i << ": "<< endl;
	for(int y=1;y<maxY-1;y++){
		for (int x=1;x<maxX-1;x++){
			cout << frontier[i][y][x];
		}
		cout << endl;
	}
	cout << endl;
}*/
    return 0;
}
