function detectPitch(buffer,sampleRate){
let energy=0,mean=0;for(let i=0;i<buffer.length;i++)mean+=buffer[i];mean/=buffer.length;
for(let i=0;i<buffer.length;i++)energy+=(buffer[i]-mean)**2;
if(Math.sqrt(energy/buffer.length)<.006)return null;
const min=Math.max(2,Math.floor(sampleRate/2000)),max=Math.min(Math.floor(sampleRate/30),Math.floor(buffer.length/2)-1),window=Math.floor(buffer.length/2),d=new Float32Array(max+1);let sum=0,chosen=-1;
for(let t=1;t<=max;t++){let diff=0;for(let j=0;j<window;j++){const v=buffer[j]-buffer[j+t];diff+=v*v;}sum+=diff;d[t]=sum?diff*t/sum:1;}
for(let t=min;t<max;t++){if(d[t]<.12){while(t+1<max&&d[t+1]<d[t])t++;chosen=t;break;}}
if(chosen<0)return null;
const a=d[chosen-1],b=d[chosen],c=d[chosen+1],den=a-2*b+c,offset=den?(a-c)/(2*den):0;
return sampleRate/(chosen+Math.max(-1,Math.min(1,offset)));
}
function pitchInfo(hz,reference=440,target=null){const exact=69+12*Math.log2(hz/reference),midi=target===null?Math.round(exact):Math.round((exact-target)/12)*12+target;return{midi,cents:(exact-midi)*100,note:['C','C♯','D','D♯','E','F','F♯','G','G♯','A','A♯','B'][((midi%12)+12)%12],octave:Math.floor(midi/12)-1};}
if(typeof module!=='undefined')module.exports={detectPitch,pitchInfo};