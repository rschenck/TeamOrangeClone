aa=dlmread('cellCycleLength.txt');
[l,w]=size(aa);
for i=1:l
    aa2=aa(i,:);aa2=aa2(aa2~=0);
    hist(aa2);
    pause(0.1);
end

xlim([10 30]);