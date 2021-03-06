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
        SPIDER_DIED("squish.mp3"),
//        SPIDER_DIED("squeek.mp3"),
        DROPPED_ANCHOR("110393__soundscalpel-com__water-splash.wav"),
        DROPPED_DIAMOND("171640__fins__scale-c7-short.mp3"),
        DROPPED_CLOCK("blurp_x.wav"),
        CIRCUIT_OFF("259961__thehorriblejoke__warp-down.mp3"),
        LASER_BREAKING("254942__deatlev__beam.wav"),
        LASER_BROKE("glass_shatter_c.wav"),
        RESHUFFLE("20216__modcam__tacs.wav"),
        FLIP_MIRROR("321104__nsstudios__blip2.wav"), //28469__simmfoc__blip-1.wav"), //191012__waterboy920__sci-fi-glitch-sound.wav"),
        LIGHT_FUSE("316682__theneedle-tv__sparkler-fuse-nm-1.wav"),
        START_ROCKET("140726__j1987__tiny-rocket-clipped.mp3"),
        MISS("125106__oddworld__sfx-tech-gui-select-01.wav"),
        EGG_CRACK("250133__fngersounds__egg-cracking_clipped.wav"),
        CHICKEN("316920__rudmer-rotteveel__chicken-single-alarm-call.wav"),
        BIRD("170811__esperar__bird-2-c_clipped.wav"),
        DRIP("25879__acclivity__drip1.wav"),
        AXE("322171__liamg-sfx__axe-impact-gore.wav"),
        ZAP("268168__shaun105__laser.wav"),
        WOOSH("216675_2549002-lq.mp3"),
        BUBBLE("89534__cgeffex__very-fast-bubble-pop1.mp3"),
        SWAP_RADIOACTIVE("28469__simmfoc__blip-1.wav"),
        PACMAN_EATS_GHOST("pacman_eatghost.wav"),
        PACMAN_CHOMP("pacman_chomp.wav"),
        SLOT_PULL("316931__timbre__lever-pull-one-armed-bandit-from-freesound-316887-by-ylearkisto.mp3"),
        CASH_REGISTER("cash_register_x.wav"),
        WIN_SLOTS("ding4.mp3"),
        
//        COIN1("coin/coin1.wav"),
//        COIN2("coin/coin2.wav"),
//        COIN3("coin/coin3.wav"),
//        COIN4("coin/coin4.wav"),
//        COIN5("coin/coin5.wav"),

        COIN1("coin/coin2.wav"),
        COIN2("coin/coin2.wav"),
        COIN3("coin/coin2.wav"),
        COIN4("coin/coin2.wav"),
        COIN5("coin/coin2.wav"),
        
        MUSIC_LOOP1("loop/Marimba Boy.wav");
//        BLIP("blip.wav");
        
        private Audio m_audio;
        private String m_file;
        
        Sound(String file)
        {
            m_file = file;
        }
        
        public void play()
        {
            play(false);
        }
        
        public void play(boolean loop)
        {
            if (m_audio == null)
            {
                m_audio = Audio.createIfSupported();
                m_audio.setSrc("sound/" + m_file);
                if (loop)
                    m_audio.setLoop(loop);
            }
            m_audio.play();
            
            MediaError err = m_audio.getError();
            if (err != null)
            {
                Debug.p(err.toSource());
            }
        }
        
        public void pause()
        {
            if (m_audio != null)
                m_audio.pause();
        }
    };
    
    private static Sound s_loop = Sound.MUSIC_LOOP1;
    
    public static void startLoop()
    {
        //s_loop.play(true);
    }
    
    public static void pauseLoop()
    {
        s_loop.pause();
    }

    private static Sound[] COINS = { Sound.COIN1, Sound.COIN2, Sound.COIN3, Sound.COIN4, Sound.COIN5 };
    
    public static void ding(int n)
    {
        COINS[n].play();
    }
}
