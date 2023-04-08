# Requires $BASE_BRANCH, which refers to the origin of this PR

baseBranch = github.branch_for_base
headBranch = github.branch_for_head
checkoutFrom = ENV["BASE_BRANCH"]
disableDanger = ENV["DISABLE_DANGER"] == "true"

markdown <<"EOS"
## Detected

| Name | Value |
| --- | --- |
| Base | #{baseBranch} |
| Head | #{headBranch} |
| CheckoutFrom | #{checkoutFrom} |
| DisableDanger | #{disableDanger} |
EOS

if disableDanger
    markdown("**Danger check is disabled.**")
else
    if baseBranch.include?("fabric") != headBranch.include?("fabric")
        fail("You're trying to merge branches for Fabric and Forge!")
    end
    
    if checkoutFrom && headBranch.include?("_")
        unless checkoutFrom == baseBranch
            fail("The base branch and target branch are not same.")
        end
        unless headBranch.include?(baseBranch)
            fail("This branch isn't for #{baseBranch}")
        end
    end
end
