/*******************************************************************************
 * Copyright 2019 metaphore
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.crashinvaders.vfx.demo.screens.demo.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.crashinvaders.vfx.common.lml.CommonLmlParser;
import com.crashinvaders.vfx.common.lml.LmlUtils;
import com.crashinvaders.vfx.common.scene2d.RepeatTextureDrawable;
import com.crashinvaders.vfx.common.scene2d.actions.ActionsExt;
import com.crashinvaders.vfx.common.scene2d.actions.TimeModulationAction;
import com.crashinvaders.vfx.common.viewcontroller.LmlViewController;
import com.crashinvaders.vfx.common.viewcontroller.ViewControllerManager;
import com.crashinvaders.vfx.scene2d.VfxWidgetGroup;
import com.github.czyzby.lml.annotation.LmlAction;

public class CanvasContentViewController extends LmlViewController {

    private final AssetManager assets;

    private WidgetGroup widgetsRoot;
    private VfxWidgetGroup vfxGroup;
    private WidgetGroup canvasTransformWrapper;
    private Image imgBackground;

    public CanvasContentViewController(ViewControllerManager viewControllers, CommonLmlParser lmlParser, AssetManager assets) {
        super(viewControllers, lmlParser);
        this.assets = assets;
    }

    @Override
    public void onViewCreated(Group sceneRoot) {
        super.onViewCreated(sceneRoot);

        vfxGroup = sceneRoot.findActor("vfxGroup");
        canvasTransformWrapper = sceneRoot.findActor("canvasTransformWrapper");
        final WidgetGroup canvasRoot = sceneRoot.findActor("canvasRoot");

        final Action backgroundAction;
        final Action logoAction;

        // Background
        {
            final RepeatTextureDrawable backgroundDrawable = new RepeatTextureDrawable(
                    assets.get("bg-scene-pattern.png", Texture.class));
            backgroundDrawable.setShift(0.0f, 0.0f);
            imgBackground = new Image(backgroundDrawable);
            imgBackground.setFillParent(true);
            canvasRoot.addActor(imgBackground);

            backgroundAction = new Action() {
                private static final float SPEED_MIN = 0.1f;
                private static final float SPEED_MAX = 0.3f;
                private static final float SPEED_DELTA = SPEED_MAX - SPEED_MIN;

                float progress;
                float speedX;
                float speedY;
                float shiftFactorX;
                float shiftFactorY;

                @Override
                public boolean act(float delta) {
                    progress = (progress + 0.3f * delta) % 1f;

                    float progressX = Math.abs(progress * 2f - 1f);
                    speedX = SPEED_MIN + progressX * SPEED_DELTA;

                    float progressY = Math.abs(((progress + 0.5f) % 1f) * 2f - 1f);
                    speedY = SPEED_MIN + progressY * SPEED_DELTA;

                    shiftFactorX -= speedX * delta;
                    shiftFactorY += speedY * delta;

                    backgroundDrawable.setShift(shiftFactorX, shiftFactorY);
                    return false;
                }
            };
        }

        // Logo
        {
            Texture texture = assets.get("gdx-vfx-logo.png");
            Image imageLogo = new Image(texture);
            imageLogo.setOrigin(Align.center);
            // Wrap into first container to setup size.
            Container containerImage = new Container<>(imageLogo);
            // Wrap into an another container to always keep composition at center of the screen.
            Container containerHolder = new Container<>(containerImage);
            containerHolder.setFillParent(true);
            canvasRoot.addActor(containerHolder);

            logoAction = ActionsExt.target(imageLogo, ActionsExt.post(Actions.sequence(
                    ActionsExt.origin(Align.center),
                    Actions.moveBy(-10f, 10f),
                    Actions.parallel(
                            Actions.forever(Actions.sequence(
                                    Actions.rotateBy(20f, 8f, Interpolation.pow3),
                                    Actions.rotateBy(-20f, 8f, Interpolation.pow3))),
                            Actions.forever(Actions.sequence(
                                    Actions.moveBy(+20f, +10f, 0.8f, Interpolation.sine),
                                    Actions.moveBy(-20f, -10f, 0.8f, Interpolation.sine))),
                            Actions.forever(Actions.sequence(
                                    Actions.scaleTo(1.25f, 1.25f),
                                    Actions.scaleTo(0.75f, 0.75f, 1.5f, Interpolation.pow2),
                                    Actions.scaleTo(1.25f, 1.25f, 1.5f, Interpolation.pow2)))
                    )
            )));
        }

        final TimeModulationAction timeModulationAction;
        canvasRoot.addAction(timeModulationAction = ActionsExt.timeModulation(Actions.parallel(
                backgroundAction,
                logoAction
        )));

        // Pause scene animation on right click.
        stage.addListener(new InputListener() {
            boolean paused = false;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (button != 1) return super.touchDown(event, x, y, pointer, button);

                paused = !paused;
                timeModulationAction.setTimeFactor(paused ? 0f : 1f);
                return true;
            }
        });

        // Add some Scene2D widgets to canvas.
        {
            lmlParser.getData().addArgument("cwMatchWidgetSize", vfxGroup.isMatchWidgetSize());
            widgetsRoot = LmlUtils.parseLmlTemplate(lmlParser, this,
                    Gdx.files.internal("lml/screen-demo/vfx-canvas-widgets.lml"));
            widgetsRoot.setFillParent(true);
            canvasRoot.addActor(widgetsRoot);
        }
    }

    @LmlAction("transformVfxCanvas") void transformVfxCanvas() {
        Actor widgetPanel = widgetsRoot.findActor("cwRightPanel");
        widgetPanel.clearActions();
        widgetPanel.setOrigin(Align.center);
        widgetPanel.addAction(Actions.sequence(
                ActionsExt.transform(true),
                Actions.scaleTo(1.3f, 1.3f, 0.15f, Interpolation.sineOut),
                Actions.scaleTo(1f, 1f, 0.75f, Interpolation.elasticOut),
                ActionsExt.transform(false)
        ));

        canvasTransformWrapper.clearActions();
        canvasTransformWrapper.setOrigin(Align.center);
        canvasTransformWrapper.addAction(Actions.sequence(
                Actions.rotateTo(0f),
                Actions.scaleTo(1f, 1f),
                Actions.parallel(
                        Actions.rotateTo(360f, 3f, Interpolation.exp10),
                        Actions.sequence(
                                Actions.scaleTo(0.6f, 0.6f, 1.5f, Interpolation.exp5In),
                                Actions.scaleTo(1.0f, 1.0f, 1.5f, Interpolation.exp5Out)
                        )
                )
        ));
    }

    @LmlAction("onMatchWidgetSizeChanged") void onMatchWidgetSizeChanged(CheckBox checkBox) {
        boolean checked = checkBox.isChecked();
        vfxGroup.setMatchWidgetSize(checked);
    }

    @LmlAction("onTransparentBackgroundChanged") void onTransparentBackgroundChanged(CheckBox checkBox) {
        boolean checked = checkBox.isChecked();
        imgBackground.setVisible(!checked);
    }
}