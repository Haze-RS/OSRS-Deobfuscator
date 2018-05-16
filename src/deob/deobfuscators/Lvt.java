/*
 * Copyright (c) 2016-2017, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package deob.deobfuscators;

import asm.ClassFile;
import asm.ClassGroup;
import asm.Method;
import asm.attributes.Code;
import asm.attributes.code.Instruction;
import asm.attributes.code.instruction.types.LVTInstruction;
import deob.Deobfuscator;
import deob.deobfuscators.lvt.Mappings;

/**
 * This deobfuscator is only required for fernflower which has a difficult time
 * when the same lvt index is used for variables of differing types (like object
 * and int), see IDEABKL-7230.
 * 
 * @author Adam
 */
public class Lvt implements Deobfuscator
{

	private int count = 0;

	private void process(Method method)
	{
		Code code = method.getCode();
		if (code == null)
		{
			return;
		}

		Mappings mappings = new Mappings(code.getMaxLocals());

		for (Instruction ins : code.getInstructions().getInstructions())
		{
			if (!(ins instanceof LVTInstruction))
			{
				continue;
			}

			LVTInstruction lv = (LVTInstruction) ins;
			Integer newIdx = mappings.remap(lv.getVariableIndex(), lv.type());

			if (newIdx == null)
			{
				continue;
			}

			assert newIdx != lv.getVariableIndex();

			Instruction newIns = lv.setVariableIndex(newIdx);
			assert ins == newIns;

			++count;
		}
	}

	@Override
	public void run(ClassGroup group)
	{
		for (ClassFile cf : group.getClasses())
		{
			for (Method m : cf.getMethods())
			{
				process(m);
			}
		}

	}

}
