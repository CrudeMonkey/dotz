package com.enno.dotz.client;

import com.enno.dotz.client.util.Debug;
import com.google.gwt.media.client.Audio;
import com.google.gwt.media.dom.client.MediaError;

public class SoundManager
{
    public enum Sound
    {
        WIN_LEVEL("274182__littlerobotsoundfactory__jingle-win-synth-05.wav"),
        OUT_OF_MOVES("172950__notr__saddertrombones.mp3"),
        CLICK("14066__adcbicycle__13.wav"),
        MADE_SQUARE("157539__nenadsimic__click.wav"),
        //CLICK("268825__kwahmah-02__clip.wav"),
        DROP("243701__ertfelda__correct.wav"),
        CLOCK_WENT("chime.wav"),
        BOMB_WENT("80500__ggctuk__exp-obj-large03.wav"),
        OUT_OF_TIME("buzzer_x.wav"),
        ANIMAL_DIED("burp_x.wav"),
        DROPPED_ANCHOR("110393__soundscalpel-com__water-splash.wav"),
        DROPPED_CLOCK("blurp_x.wav"),
        CIRCUIT_OFF("259961__thehorriblejoke__warp-down.mp3"),
        LASER_BREAKING("254942__deatlev__beam.wav"),
        LASER_BROKE("glass_shatter_c.wav"),
        RESHUFFLE("20216__modcam__tacs.wav"),
        FLIP_MIRROR("321104__nsstudios__blip2.wav"), //28469__simmfoc__blip-1.wav"), //191012__waterboy920__sci-fi-glitch-sound.wav"),
        LIGHT_FUSE("316682__theneedle-tv__sparkler-fuse-nm-1.wav"),
        START_ROCKET("140726__j1987__tiny-rocket-clipped.mp3"),
        MISS("125106__oddworld__sfx-tech-gui-select-01.wav"),
        ZAP("268168__shaun105__laser.wav");
//        BLIP("blip.wav");
        
        private Audio m_audio;
        private String m_file;
        
        Sound(String file)
        {
            m_file = file;
        }
        
        public void play()
        {
            if (m_audio == null)
            {
                m_audio = Audio.createIfSupported();
                m_audio.setSrc("sound/" + m_file);
            }
            m_audio.play();
            
            MediaError err = m_audio.getError();
            if (err != null)
            {
                Debug.p(err.toSource());
            }
        }
    };
}
