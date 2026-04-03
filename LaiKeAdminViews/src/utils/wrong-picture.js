import ErrorImg from "@/assets/images/default_picture.png";

// 图片错误处理
export function handleErrorImg(e){
  console.log("图片报错了", e.target.src);
  e.target.src = ErrorImg;
};
