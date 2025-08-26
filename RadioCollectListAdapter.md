展示收藏的电台
```java
@Override  
public void onBindViewHolder(ViewHolder holder, int position) {  
    RadioMessage radioMessage = mRadioMessageList.get(position);  
    Log.d(TAG, "onBindViewHolder: radioMessage = " + radioMessage);  
    if (hasMulti){  
        holder.tvMultiType.setVisibility(View.VISIBLE);  
    }else {  
        holder.tvMultiType.setVisibility(View.GONE);  
    }  
    String frequency = "";  
    if (radioMessage.getRadioType() == RadioMessage.FM_AM_TYPE) {  
        switch (radioMessage.getRadioBand()) {  
            case RadioManager.BAND_AM:  
                frequency = frequency + String.format(Locale.ENGLISH,mContext.getResources().getString(R.string.radio_am_item_title),radioMessage.getRadioFrequency());  
                holder.tvMultiType.setText("AM");  
                break;  
            case RadioManager.BAND_FM:  
                //Android 原生Bug,俄语环境下，“.”会被变成","  
                //FM+频点值在各个语言下是一样的显示，因此强制使用英语环境显示这个字串  
                frequency = frequency + String.format(Locale.ENGLISH,mContext.getResources().getString(R.string.radio_fm_item_title),radioMessage.getRadioFrequency() / 1000.0);  
                holder.tvMultiType.setText("FM");  
                break;  
        }  
    }else {  
        frequency = radioMessage.getDabMessage().getShortProgramStationName();  
        holder.tvRadioName.setText(radioMessage.getDabMessage().getShortEnsembleLabel());  
        holder.tvMultiType.setText("DAB");  
    }  
    holder.tvFreq.setText(frequency);  
    RadioMessage currentRadioMessage = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage();  
    //更新当前播放item的图标  
    if (radioMessage.getRadioBand() == currentRadioMessage.getRadioBand()  
            && radioMessage.getRadioFrequency() == currentRadioMessage.getRadioFrequency()) {  
        Log.d(TAG, "onBindViewHolder: mCurrentRadioMessage = " + radioMessage);  
        if (radioMessage.getRadioType() == RadioMessage.DAB_TYPE){  
            //DAB需要多加判断  
            if (radioMessage.getDabMessage().getServiceId() == currentRadioMessage.getDabMessage().getServiceId()  
                && radioMessage.getDabMessage().getServiceComponentId() == currentRadioMessage.getDabMessage().getServiceComponentId()){  
                currentRadioPosition = position;  
                holder.ivItemIcon.setSelected(true);  
                holder.ivItemPlay.setVisibility(View.VISIBLE);  
                holder.ivItemPlayBg.setVisibility(View.VISIBLE);  
                if (ModuleRadioTrigger.getInstance().mGetRadioStatusTool.isPlaying()) {  
                    holder.ivItemPlay.start();  
                } else {  
                    holder.ivItemPlay.stop();  
                }  
                //优先使用Sls  
                byte[] logoDataList = DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage().getDabMessage().getSlsDataList();  
                //次级使用存储的Logo  
                if (logoDataList == null || logoDataList.length < 1){  
                    logoDataList = ListUtils.getOppositeDABLogo(radioMessage);  
                }  
                //最后使用当前获取到的Logo  
                if (logoDataList == null){  
                    logoDataList = DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage().getDabMessage().getLogoDataList();  
                }  
                if (logoDataList != null && logoDataList.length > 0){  
                    holder.ivItemIcon.setImageBitmap(BitmapFactory.decodeByteArray(logoDataList, 0, logoDataList.length));  
                }else {  
                    holder.ivItemIcon.setImageResource(com.desaysv.moduledab.R.mipmap.icon_logo);  
                }  
            }else {  
                holder.ivItemIcon.setSelected(false);  
                holder.ivItemPlay.setVisibility(View.GONE);  
                holder.ivItemPlayBg.setVisibility(View.GONE);  
                byte[] logoDataList = logoDataList = ListUtils.getOppositeDABLogo(radioMessage);  
                if (logoDataList != null && logoDataList.length > 0){  
                    holder.ivItemIcon.setImageBitmap(BitmapFactory.decodeByteArray(logoDataList, 0, logoDataList.length));  
                }else {  
                    holder.ivItemIcon.setImageResource(com.desaysv.moduledab.R.mipmap.icon_logo);  
                }  
            }  
  
        }else {  
            currentRadioPosition = position;  
            holder.ivItemIcon.setSelected(true);  
            holder.ivItemPlay.setVisibility(View.VISIBLE);  
            holder.ivItemPlayBg.setVisibility(View.VISIBLE);  
            holder.ivItemIcon.setImageResource(com.desaysv.moduledab.R.mipmap.icon_logo);  
            if (ModuleRadioTrigger.getInstance().mGetRadioStatusTool.isPlaying()) {  
                holder.ivItemPlay.start();  
            } else {  
                holder.ivItemPlay.stop();  
            }  
            String radioName = RadioCovertUtils.getOppositeRDSName(radioMessage) != null ? RadioCovertUtils.getOppositeRDSName(radioMessage) : null;  
            if (currentRadioMessage.getRdsRadioText() != null && radioName == null){  
                radioName = currentRadioMessage.getRdsRadioText().getProgramStationName();  
            }  
            holder.tvRadioName.setText((radioName == null || radioName.length() < 1 || radioName.trim().length() < 1) ? mContext.getResources().getString(R.string.radio_station_name): radioName);  
            if (hasMulti) {  
                holder.tvFreq.setText((radioName == null || radioName.length() < 1 || radioName.trim().length() < 1) ? frequency : radioName);  
            }  
        }  
    } else {  
        holder.ivItemIcon.setSelected(false);  
        holder.ivItemPlay.setVisibility(View.GONE);  
        holder.ivItemPlayBg.setVisibility(View.GONE);  
        String radioName = RadioCovertUtils.getOppositeRDSName(radioMessage) != null ? RadioCovertUtils.getOppositeRDSName(radioMessage) : null;  
        holder.tvRadioName.setText((radioName == null || radioName.length() < 1 || radioName.trim().length() < 1) ? mContext.getResources().getString(R.string.radio_station_name): radioName);  
        if(hasMulti) {  
            holder.tvFreq.setText((radioName == null || radioName.length() < 1 || radioName.trim().length() < 1) ? frequency : radioName);  
        }  
        if (radioMessage.getRadioType() == RadioMessage.DAB_TYPE) {  
            holder.tvRadioName.setText(radioMessage.getDabMessage().getShortEnsembleLabel());  
            byte[] logoDataList = ListUtils.getOppositeDABLogo(radioMessage);  
            if (logoDataList != null && logoDataList.length > 0) {  
                holder.ivItemIcon.setImageBitmap(BitmapFactory.decodeByteArray(logoDataList, 0, logoDataList.length));  
            } else {  
                holder.ivItemIcon.setImageResource(com.desaysv.moduledab.R.mipmap.icon_logo);  
            }  
        }else {  
            holder.ivItemIcon.setImageResource(com.desaysv.moduledab.R.mipmap.icon_logo);  
        }  
    }  
}
```